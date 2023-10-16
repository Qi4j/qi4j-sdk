/*
* Copyright 2008-2023 Qi4j Community (see commit log). All Rights Reserved
*
* Licensed  under the  Apache License,  Version 2.0  (the "License");
* you may not use  this file  except in  compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed  under the  License is distributed on an "AS IS" BASIS,
* WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.gradle.structure.release

import org.qi4j.gradle.BasePlugin
import org.qi4j.gradle.TaskGroups
import org.qi4j.gradle.structure.distributions.DistributionsPlugin
import org.qi4j.gradle.structure.manual.ManualPlugin
import org.qi4j.gradle.structure.reports.ReportsPlugin
import groovy.transform.CompileStatic
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip
import org.gradle.language.base.plugins.LifecycleBasePlugin

@CompileStatic
class ReleasePlugin implements Plugin<Project>
{
  static class TaskNames
  {
    static final String RELEASE_QI4J = 'releaseQi4j'
    static final String PUBLISH_QI4J_MAVEN = 'publishQi4jMavenArtifacts'
    private static final String PREPARE_QI4j_MAVEN = 'prepareQi4jMavenBundle'
    private static final String UPLOAD_QI4J_MAVEN = 'uploadQi4jMavenBundle'
    private static final String CLOSE_QI4J_MAVEN = 'closeQi4jMavenRepository'
    private static final String CHECK_QI4J_MAVEN = 'checkQi4jMavenArtifacts'
    private static final String PROMOTE_QI4J_MAVEN = 'promoteQi4jMavenRepository'
    static final String PUBLISH_QI4J_DIST = 'publishQi4jDistributions'
    private static final String CHECKOUT_QI4J_DIST = 'checkoutQi4jDistributions'
    private static final String COPY_QI4J_DIST = 'copyQi4jDistributions'
    private static final String COMMIT_QI4J_DIST = 'commitQi4jDistributions'
    static final String PUBLISH_QI4J_DOC = 'publishQi4jDocumentation'
    private static final String CHECKOUT_QI4J_DOC = 'checkoutQi4jDocumentation'
    private static final String COPY_QI4J_DOC = 'copyQi4jDocumentation'
    private static final String COPY_QI4J_DOC_LATEST = 'copyQi4jDocumentationAsLatest'
    private static final String COMMIT_QI4J_DOC = 'commitQi4jDocumentation'
  }

  @Override
  void apply( final Project project )
  {
    project.plugins.apply BasePlugin
    project.gradle.taskGraph.whenReady { TaskExecutionGraph taskGraph ->
      def check = taskGraph.allTasks.any { task -> task.name.contains( 'Qi4j' ) }
      if( check )
      {
        checkQi4jPreconditions( project )
      }
    }
    applyQi4jRelease project
  }

  static void checkQi4jPreconditions(Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    if( releaseSpec.developmentVersion )
    {
      throw new InvalidUserDataException(
        'Development version is unreleasable, please clean and retry with a -Dversion=' )
    }
    def qi4jWeb = new File( project.rootProject.projectDir.parentFile, 'qi4j-website' )
    if( !qi4jWeb.exists() )
    {
      throw new InvalidUserDataException(
        'To perform Qi4j releases you need to clone the `qi4j-website` repository under ../qi4j-website' )
    }
    def qi4jDist = new File( project.rootProject.projectDir.parentFile, 'qi4j-dist' )
    if( !qi4jDist.exists() )
    {
      throw new InvalidUserDataException(
        'To perform Qi4j releases you need to checkout the SVN dist directory under ../qi4j-dist' )
    }
    // TODO Check Nexus credentials availability
    // TODO Check svn command line availability
  }

  static void applyQi4jRelease(Project project )
  {
    Task releaseTask = project.tasks.create( TaskNames.RELEASE_QI4J ) { Task task ->
      task.group = TaskGroups.RELEASE
      task.description = 'Rolls out an Qi4j release.'
    }
    def subTasks = [
            applyPublishQi4jMavenArtifacts( project ),
            applyPublishQi4jDistributions( project ),
            applyPublishQi4jDocumentation( project )
    ]
    // Two upload strategies for now
    if( project.findProperty( 'useMavenBundle' ) )
    {
      // Use maven artifact bundle
      releaseTask.dependsOn subTasks
    }
    else
    {
      // Use :**:uploadArchives for now
      // TODO Remove this once the bundle strategy is done
      def releaseSpec = project.extensions.getByType ReleaseSpecExtension
      releaseSpec.publishedProjects.each { p ->
        releaseTask.dependsOn "${ p.path }:uploadArchives"
      }
      releaseTask.dependsOn subTasks.drop( 1 )
    }
  }

  static Task applyPublishQi4jMavenArtifacts(Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    def distributions = project.rootProject.project ':distributions'
    def prepare = project.tasks.create( TaskNames.PREPARE_QI4j_MAVEN, Zip ) { Zip task ->
      // TODO Consume distributions through configurations
      task.dependsOn "${ distributions.path }:${ DistributionsPlugin.TaskNames.STAGE_MAVEN_BINARIES }"
      task.from "${ distributions.buildDir }/stage/maven-binaries"
      task.into '.'
      task.destinationDirectory.set( project.file( "$project.buildDir/asf/maven" ) )
      task.archiveBaseName.set( "qi4j-${ project.version }-maven-artifacts".toString() )
      task.exclude '**/maven-metadata*.*'
    }
    def upload = project.tasks.create( TaskNames.UPLOAD_QI4J_MAVEN ) { Task task ->
      task.dependsOn prepare
      task.doLast {
        def uploadUrl = releaseSpec.releaseVersion ?
                        'https://qi4j.github.com/repository/maven-snapshots/' :
                        'https://qi4j.github.com/repository/maven-releases/'
        CloseableHttpClient httpClient = HttpClients.createDefault()
        try
        {
          // TODO Add Nexus Authentication
          HttpPost post = new HttpPost( "$uploadUrl/content-compressed" )
          MultipartEntityBuilder builder = MultipartEntityBuilder.create()
          builder.addBinaryBody( 'fieldname',
                                 prepare.archiveFile.get().asFile,
                                 ContentType.APPLICATION_OCTET_STREAM,
                                 prepare.archiveFile.get().asFile.getName() )
          post.setEntity( builder.build() )
          CloseableHttpResponse response = httpClient.execute( post )
          if( response.statusLine.statusCode != 200 )
          {
            throw new GradleException( "Unable to upload maven artifacts to Github, got ${ response.statusLine }" )
          }
        }
        finally
        {
          httpClient.close()
        }
      }
    }

    def close = project.tasks.create( TaskNames.CLOSE_QI4J_MAVEN ) { Task task ->
      task.mustRunAfter upload
      // TODO Close Nexus repository
      task.enabled = false
    }

    def check = project.tasks.create( TaskNames.CHECK_QI4J_MAVEN ) { Task task ->
      task.mustRunAfter close
      // TODO Run tests against binaries from Nexus staged repository
      task.enabled = false
    }

    def promote = project.tasks.create( TaskNames.PROMOTE_QI4J_MAVEN ) { Task task ->
      task.mustRunAfter check
      // TODO Promote Nexus repository
      task.enabled = false
    }

    def publish = project.tasks.create( TaskNames.PUBLISH_QI4J_MAVEN ) { Task task ->
      task.group = TaskGroups.RELEASE
      task.description = 'Publishes maven artifacts.'
      task.dependsOn upload, close, check, promote
    }

    return publish
  }

  static Task applyPublishQi4jDistributions(Project project )
  {
    def distributions = project.rootProject.project ':distributions'
    def checkout = project.tasks.create( TaskNames.CHECKOUT_QI4J_DIST ) { Task task ->
      task.enabled = false
    }
    // TODO Split website and javadoc copy and use Sync task instead
    def copy = project.tasks.create( TaskNames.COPY_QI4J_DIST, Copy ) { Copy task ->
      task.mustRunAfter checkout
      // TODO Consume distributions through configurations
      task.dependsOn "${ distributions.path }:${ LifecycleBasePlugin.ASSEMBLE_TASK_NAME }"
      task.from new File( distributions.buildDir, 'distributions' )
      task.into new File( project.rootProject.projectDir.parentFile, 'qi4j-dist/dev/qi4j' )
    }
    def commit = project.tasks.create( TaskNames.COMMIT_QI4J_DIST ) { Task task ->
      task.mustRunAfter copy
      task.enabled = false
    }
    def publish = project.tasks.create( TaskNames.PUBLISH_QI4J_DIST ) { Task task ->
      task.group = TaskGroups.RELEASE
      task.description = 'Publishes distributions.'
      task.dependsOn checkout, copy, commit
    }
    // TODO SVN Upload DISTRIBUTIONS using svn command line so credentials are handled outside of the build
    return publish
  }

  static Task applyPublishQi4jDocumentation(Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    def manual = project.rootProject.project ':manual'
    def reports = project.rootProject.project ':reports'
    def checkout = project.tasks.create( TaskNames.CHECKOUT_QI4J_DOC ) { Task task ->
      task.enabled = false
    }
    def copy = project.tasks.create( TaskNames.COPY_QI4J_DOC, Copy ) { Copy task ->
      task.mustRunAfter checkout
      // TODO Consume documentation and reports through configurations
      task.dependsOn "${ manual.path }:${ ManualPlugin.TaskNames.WEBSITE }"
      task.dependsOn "${ reports.path }:${ ReportsPlugin.TaskNames.JAVADOCS }"
      def webRoot = new File( project.rootProject.projectDir.parentFile, 'qi4j-website' )
      def dirName = releaseSpec.releaseVersion ? project.version : 'develop'
      task.destinationDir = webRoot
      task.from( new File( manual.buildDir, 'docs/website' ) ) { CopySpec spec ->
        spec.into "content/java/$dirName"
      }
      task.from( new File( reports.buildDir, 'docs/javadocs' ) ) { CopySpec spec ->
        spec.into "content/java/$dirName/javadocs"
      }
    }
    project.tasks.create( TaskNames.COPY_QI4J_DOC_LATEST, Copy ) { Copy task ->
      def webRoot = new File( project.rootProject.projectDir.parentFile, 'qi4j-website' )
      task.from new File( webRoot, "content/java/$project.version" )
      task.into new File( webRoot, "content/java/latest" )
      task.doFirst {
        if( !releaseSpec.releaseVersion )
        {
          throw new InvalidUserDataException( 'Development version cannot be `latest`.' )
        }
      }
    }
    def commit = project.tasks.create( TaskNames.COMMIT_QI4J_DOC ) { Task task ->
      task.mustRunAfter copy
      task.enabled = false
    }
    def publish = project.tasks.create( TaskNames.PUBLISH_QI4J_DOC ) { Task task ->
      task.group = TaskGroups.RELEASE
      task.description = 'Publishes documentation.'
      task.dependsOn checkout, copy, commit
    }
    return publish
  }
}
