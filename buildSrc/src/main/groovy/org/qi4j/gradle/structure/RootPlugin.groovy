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
package org.qi4j.gradle.structure

import groovy.transform.CompileStatic
import org.qi4j.gradle.BasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.util.GradleVersion
import org.qi4j.gradle.dependencies.DependenciesDeclarationExtension

@CompileStatic
class RootPlugin implements Plugin<Project>
{
  static final String PROJECT_TITLE = 'Qi4j SDK'
  static final String PROJECT_DESCRIPTION = 'Qi4j is a framework for domain centric ' +
                                            'application development, including evolved concepts from AOP, DI and DDD.'

  @Override
  void apply( Project project )
  {
    project.plugins.apply BasePlugin
    applyProjectMetadata( project )
    applyGradleWrapper( project )
  }

  private static void applyProjectMetadata( Project project )
  {
    def extraProperties = project.extensions.extraProperties
    extraProperties.set 'title', PROJECT_TITLE
    extraProperties.set 'description', PROJECT_DESCRIPTION
  }

  private static void applyGradleWrapper( Project project )
  {
    def dependencies = project.extensions.getByType( DependenciesDeclarationExtension )
    def requiredGradleVersion = GradleVersion.version( dependencies.gradleVersion )
    def currentGradleVersion = GradleVersion.current()
    if( currentGradleVersion != requiredGradleVersion ) {
      def warning = "The Qi4j build is not supported with $currentGradleVersion. " +
                    "The only supported version is $requiredGradleVersion."
      project.logger.error( warning )
      project.gradle.buildFinished {
        project.logger.error( warning )
      }
    }
    project.tasks.create( 'wrapper', Wrapper) { Wrapper wrapper ->
      wrapper.gradleVersion = dependencies.gradleVersion
      wrapper.distributionType = Wrapper.DistributionType.ALL
    }
  }
}
