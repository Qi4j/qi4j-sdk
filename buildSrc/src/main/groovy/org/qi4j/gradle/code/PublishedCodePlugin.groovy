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
package org.qi4j.gradle.code

import org.qi4j.gradle.structure.manual.AsciidocBuildInfoPlugin
import org.qi4j.gradle.structure.release.ReleaseSpecExtension
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPomLicense
import org.gradle.api.publish.maven.MavenPomLicenseSpec
import org.gradle.api.publish.maven.MavenPomScm
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.jvm.tasks.Jar

@CompileStatic
class PublishedCodePlugin implements Plugin<Project> {
  private MavenPublication publication

  @Override
  void apply(final Project project) {
    configurePublishing project
    project.plugins.apply CodePlugin
    configureJavadoc project
    configureHonker(project)
//    project.plugins.apply VersionClassPlugin
    project.plugins.apply AsciidocBuildInfoPlugin
    applySupplementaryArchives project
    configurePublication project
//    project.plugins.apply PublishingPlugin
  }

  private void configurePublishing(Project project) {
    project.plugins.apply MavenPublishPlugin
    def publicationName = PublishNaming.publishedNameFor(project.path)
    def publishing = project.extensions.getByType(PublishingExtension)
    publication = publishing.publications.create "$publicationName", MavenPublication
    publishing.repositories { RepositoryHandler r ->
      r.maven() { MavenArtifactRepository repo ->
        if (project.version == "unspecified") {
          repo.url = "https://qi4j.github.com/repository/maven-snapshots/"
        } else {
          repo.url = "https://qi4j.github.com/repository/maven-releases/"
        }
        repo.credentials {
          it.username = "niclas"
          it.password = "RottenEgg!"
        }
      }
    }
  }

  private void configurePublication(Project project) {
    publication.with { MavenPublication pub ->
      from project.components.getByName("java")
      pub.artifactId = PublishNaming.publishedNameFor(project.path)
      pub.pom { MavenPom pom ->
        pom.name.set(project.name)
        pom.description.set(project.description)
        pom.url.set("https://qi4j.github.com")
        pom.licenses {
          MavenPomLicenseSpec licSpec ->
            licSpec.license { MavenPomLicense lic ->
              lic.name.set "Proprietary License. Contact https://qi4j.github.com"
              lic.url.set "https://qi4j.github.com/license"
            }
        }
        pom.scm { MavenPomScm scm ->
          scm.connection.set("https://github.com/Qi4j/qi4j-sdk")
          scm.developerConnection.set("ssh://git@github.com:Qi4j/qi4j-sdk.git")
          scm.url.set("https://github.com/Qi4j/qi4j-sdk")
        }
      }
    }
  }

  static void configureJavadoc(Project project) {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    project.tasks.withType(Javadoc) { Javadoc task ->
      task.onlyIf { !releaseSpec.developmentVersion }
      def options = task.options as StandardJavadocDocletOptions
      options.encoding = 'UTF-8'
      options.docEncoding = 'UTF-8'
      options.charSet = 'UTF-8'
      options.noTimestamp = true
      options.links = [
              'https://docs.oracle.com/en/java/javase/14/docs/api/',
              'https://stleary.github.io/JSON-java/',
              'https://junit.org/junit4/javadoc/latest/'
      ]
      // exclude '**/internal/**'
    }
  }

  private static void configureHonker(Project project) {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    if (releaseSpec.developmentVersion) {
      return
    }
// TODO (Paul?)
/*
    def honkerGenDependencies = project.tasks.getByName('honkerGenDependencies') as HonkerGenDependenciesTask
    def honkerGenLicense = project.tasks.getByName('honkerGenLicense') as HonkerGenLicenseTask
    def honkerGenNotice = project.tasks.getByName('honkerGenNotice') as HonkerGenNoticeTask
    def honkerCheck = project.tasks.getByName('honkerCheck') as HonkerCheckTask
    [honkerGenDependencies, honkerGenLicense, honkerGenNotice, honkerCheck].group = null
    def javaExtension = project.extensions.getByType JavaPluginExtension
    def mainSourceSet = javaExtension.sourceSets.getByName 'main'
    mainSourceSet.output.dir([builtBy: honkerGenDependencies] as Map<String, Object>,
            honkerGenDependencies.outputDir)
    mainSourceSet.output.dir([builtBy: honkerGenLicense] as Map<String, Object>,
            honkerGenLicense.outputDir)
    mainSourceSet.output.dir([builtBy: honkerGenNotice] as Map<String, Object>,
            honkerGenNotice.outputDir)
    def honker = project.extensions.getByType HonkerExtension
    // Project License, applied to all submodules
    honker.license 'Apache 2'
    // Dependencies (transitive or not) with no license information, overriding them
    honker.licenseOverride { HonkerLicenseOverrideCandidate candidate ->
      if (candidate.group == 'asm' || candidate.module == 'prefuse-core') {
        candidate.license = 'BSD 3-Clause'
      }
      if (candidate.group == 'com.github.jnr') {
        candidate.license = 'EPL'
      }
      if (candidate.group == 'javax.json'
              || candidate.group == 'javax.websocket'
              || candidate.group == 'javax.xml.bind') {
        candidate.license = 'CDDL'
      }
      if (candidate.group == 'org.apache.httpcomponents'
              || candidate.group == 'net.java.dev.jna'
              || candidate.group == 'lucene'
              || candidate.group == 'org.osgi'
              || candidate.group.startsWith('org.restlet')) {
        candidate.license = 'Apache 2'
      }
    }
    honkerGenNotice.header = 'Qi4j'
    honkerGenNotice.footer = 'This product includes software developed at\n' +
            'The Apache Software Foundation (https://www.apache.org/).\n'
    project.tasks.getByName('check').dependsOn honkerCheck

 */
  }

  private void applySupplementaryArchives(Project project) {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    def javaExtension = project.extensions.getByType JavaPluginExtension
    def sourceJar = project.tasks.create('sourceJar', Jar) { Jar task ->
      task.description = 'Builds -sources.jar'
      task.archiveClassifier.set('sources')
      task.from javaExtension.sourceSets.getByName('main').allSource
    }
    def testSourceJar = project.tasks.create('testSourceJar', Jar) { Jar task ->
      task.description = 'Builds -testsources.jar'
      task.archiveClassifier.set('testsources')
      task.onlyIf { !releaseSpec.developmentVersion }
      task.from javaExtension.sourceSets.getByName('test').allSource
    }
    def javadoc = project.tasks.getByName('javadoc') as Javadoc
    def javadocJar = project.tasks.create('javadocJar', Jar) { Jar task ->
      task.description = 'Builds -javadoc.jar'
      task.archiveClassifier.set('javadoc')
      task.onlyIf { !releaseSpec.developmentVersion }
      task.from javadoc.destinationDir
      task.dependsOn javadoc
    }
    project.artifacts.add 'archives', sourceJar
    publication.artifact sourceJar

    if (!releaseSpec.developmentVersion) {
      project.artifacts.add 'archives', testSourceJar
      project.artifacts.add 'archives', javadocJar
      publication.artifact testSourceJar
      publication.artifact javadocJar
    }
  }
}
