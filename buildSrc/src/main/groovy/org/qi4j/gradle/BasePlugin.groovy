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
package org.qi4j.gradle

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.qi4j.gradle.dependencies.Qi4jExtension
import org.qi4j.gradle.structure.release.ReleaseSpecPlugin

/**
 * Plugin applied to all Qi4j projects.
 */
@CompileStatic
class BasePlugin implements Plugin<Project>
{
  @Override
  void apply( Project project )
  {
    applyGroup project
    applyVersion project
    project.plugins.apply ReleaseSpecPlugin
    applyQi4jExtension project
    project.plugins.apply LifecycleBasePlugin
    project.defaultTasks 'classes', 'test'
  }

  private static void applyGroup( Project project )
  {
    project.group = project.path == ':' ?
                    'org.qi4j' :
                    "org.qi4j.${ project.path.split( ':' ).drop( 1 ).dropRight( 1 ).join( '.' ) }"
  }

  private static void applyVersion( Project project )
  {
    if( project.version == 'unspecified' )
    {
      project.version = System.properties.version ?: '0'
    }
  }

  private static void applyQi4jExtension(Project project )
  {
    project.extensions.create 'qi4j', Qi4jExtension, project
  }
}
