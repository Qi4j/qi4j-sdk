/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.qi4j.gradle.code

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployer
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.internal.plugins.DslObject
import org.gradle.api.plugins.MavenRepositoryHandlerConvention
import org.gradle.api.publication.maven.internal.deployer.DefaultGroovyMavenDeployer
import org.gradle.api.publication.maven.internal.deployer.MavenRemoteRepository
import org.gradle.api.tasks.Upload
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.qi4j.gradle.structure.release.ReleaseSpecExtension

/**
 * Publishing.
 *
 * <strong>Configuration</strong>
 *
 * By default RELEASES are signed, SNAPSHOTS are not.
 * Signing can be turned on or off by setting the {@literal uploadSigned} property.
 *
 * By default RELEASES must satisfy {@link org.qi4j.gradle.structure.release.ModuleReleaseSpec}, SNAPSHOT don't.
 * This can be turned on or off by setting the {@literal uploadReleaseSpec} property.
 *
 * By default RELEASES and SNAPSHOTS are uploaded using HTTP.
 * Used Wagon can be overridden by setting the {@literal uploadWagon} property.
 *
 * By default RELEASES and SNAPSHOTS are uploaded to Apache Nexus.
 * Target repository can be overridden by setting the {@literal uploadRepository} property.
 *
 * No username/password is provided by default.
 * If needed set them using the uploadUsername and {@literal uploadPassword} properties.
 */
@CompileStatic
class PublishingPlugin implements Plugin<Project>
{
  static final String WAGON_HTTP = 'org.apache.maven.wagon:wagon-http:2.2'
  static final String WAGON_SSH = 'org.apache.maven.wagon:wagon-ssh:2.2'
  static final String WAGON_WEBDAV = 'org.apache.maven.wagon:wagon-webdav:1.0-beta-2'

  static final String RELEASES_REPOSITORY_NAME = 'apache.releases.https'
  static final String RELEASES_REPOSITORY_URL = 'https://repository.apache.org/service/local/staging/deploy/maven2'
  static final String SNAPSHOTS_REPOSITORY_NAME = 'apache.snapshots.https'
  static final String SNAPSHOTS_REPOSITORY_URL = 'https://repository.apache.org/content/repositories/snapshots'

  static class Config
  {
    boolean snapshots
    boolean releases
    boolean signed
    boolean releaseSpec
    String wagon
    String repositoryName
    String repositoryUrl
    String username
    String password
  }

  @Override
  void apply( final Project project )
  {
    Config config = configFor project
    applyWagonConfiguration project, config
    configureSigning project, config
    configureUploadArchives project, config
    configureStageArchives project
    configureMavenMetadata project
    applyMavenPublishAuth project
  }

  private static Config configFor( Project project )
  {
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    def config = new Config()
    config.snapshots = releaseSpec.snapshotVersion
    config.releases = releaseSpec.releaseVersion
    config.signed = project.findProperty( 'uploadSigned' ) ?: config.releases
    config.releaseSpec = project.findProperty( 'uploadReleaseSpec' ) ?: config.releases
    config.wagon = project.findProperty( 'uploadWagon' ) ?: WAGON_HTTP
    config.repositoryName = project.findProperty( 'uploadRepositoryName' ) ?:
                            config.releases ? RELEASES_REPOSITORY_NAME : SNAPSHOTS_REPOSITORY_NAME
    config.repositoryUrl = project.findProperty( 'uploadRepository' ) ?:
                           config.releases ? RELEASES_REPOSITORY_URL : SNAPSHOTS_REPOSITORY_URL
    config.username = project.findProperty( 'uploadUsername' )
    config.password = project.findProperty( 'uploadPassword' )
    return config
  }

  private static void applyWagonConfiguration( Project project, Config config )
  {
    project.configurations.create( 'deployersJars' )
    project.dependencies.add( 'deployersJars', config.wagon )
  }

  private static void configureSigning( Project project, Config config )
  {
    project.plugins.apply 'signing'
    def signing = project.extensions.getByType SigningExtension
    signing.required = config.signed && !project.findProperty( 'skipSigning' )
    signing.sign project.configurations.getByName( 'archives' )
    def signArchives = project.tasks.getByName( 'signArchives' ) as Sign
    signArchives.enabled = config.signed && !project.findProperty( 'skipSigning' )
    signArchives.onlyIf { !project.findProperty( 'skipSigning' ) }
  }

  @CompileStatic( TypeCheckingMode.SKIP )
  private static void configureUploadArchives( Project project, Config config )
  {
    project.plugins.apply 'maven'
    def releaseSpec = project.extensions.getByType ReleaseSpecExtension
    def uploadArchives = project.tasks.getByName( 'uploadArchives' ) as Upload
    uploadArchives.doFirst {
      if( !config.snapshots && !config.releases )
      {
        throw new GradleException( "'version' must be given as a system property to perform a release." )
      }
    }
    uploadArchives.onlyIf {
      def notSkipped = !project.hasProperty( 'skipUpload' )
      def approvedProject = releaseSpec.publishedProjects.contains project
      return notSkipped && ( !config.releaseSpec || ( approvedProject || project == project.rootProject ) )
    }
    uploadArchives.dependsOn project.tasks.getByName( 'check' )
    def mavenDeployer = getMavenDeployer uploadArchives
    if( config.signed )
    {
      mavenDeployer.beforeDeployment { MavenDeployment deployment ->
        project.extensions.getByType( SigningExtension ).signPom deployment
      }
    }
    mavenDeployer.configuration = project.configurations.getByName( 'deployersJars' )
    def repoCoordinates = [ id: config.repositoryName, url: config.repositoryUrl ] as Map
    def repoCredentials = [ userName: config.username, password: config.password ] as Map
    def repo = mavenDeployer.createRepository(repoCoordinates)
    if( config.username )
    {
      ( repo as MavenRemoteRepository ).authentication( repoCredentials )
    }
    if( config.releases )
    {
      mavenDeployer.repository = repo
    }
    else
    {
      mavenDeployer.snapshotRepository = repo
    }
  }

  private static void configureStageArchives( Project project )
  {
    def stageConfiguration = project.configurations.create 'stageArchives'
    stageConfiguration.extendsFrom project.configurations.getByName( 'archives' )

    def stageTask = project.tasks.getByName( 'uploadStageArchives' ) as Upload
    stageTask.uploadDescriptor = true
    def mavenDeployer = getMavenDeployer stageTask
    def repository = new MavenRemoteRepository()
    repository.id = 'stageArchives'
    repository.url = "file://${ project.buildDir.absolutePath }/stage/archives"
    mavenDeployer.repository = repository
    mavenDeployer.snapshotRepository = repository
  }

  private static void configureMavenMetadata( Project project )
  {
    project.tasks.withType( Upload ) { Upload task ->
      def mavenDeployer = task.repositories.findByName( 'mavenDeployer' ) as MavenDeployer
      if( mavenDeployer )
      {
        MavenMetadata.applyTo mavenDeployer
        mavenDeployer.pom.artifactId = PublishNaming.publishedNameFor( project.path )
      }
    }
  }

  private static void applyMavenPublishAuth( final Project project )
  {
    // Bug in maven-publish-auth require apply after uploadArchives setup
    project.plugins.apply 'maven-publish-auth'
  }

  private static DefaultGroovyMavenDeployer getMavenDeployer( Upload uploadTask )
  {
    // TODO Remove use of Gradle internals
    // DslObject to get the Upload task convention
    // MavenRepositoryHandlerConvention & DefaultGroovyMavenDeployer to configure Wagon, Authentication and Signing
    def repositoriesConvention = new DslObject( uploadTask.repositories )
      .getConvention()
      .getPlugin( MavenRepositoryHandlerConvention )
    return repositoriesConvention.mavenDeployer() as DefaultGroovyMavenDeployer
  }
}
