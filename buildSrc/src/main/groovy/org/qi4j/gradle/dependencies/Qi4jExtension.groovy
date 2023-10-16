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
package org.qi4j.gradle.dependencies

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

@CompileStatic
class Qi4jExtension
{
  private final Project project
  final Core core

  Qi4jExtension(Project project )
  {
    this.project = project
    this.core = new Core()
  }

  class Core
  {
    Dependency api = core( 'api' )
    Dependency spi = core( 'spi' )
    Dependency runtime = core( 'runtime' )
    Dependency bootstrap = core( 'bootstrap' )
    Dependency testsupport = core( 'testsupport' )
  }

  private Dependency core( String name )
  {
    return dependency( 'core', name )
  }

  Dependency library( String name )
  {
    return dependency( 'libraries', name )
  }

  Dependency extension( String name )
  {
    return dependency( 'extensions', name )
  }

  Dependency service( String name )
  {
    return dependency( 'services', name )
  }

  Dependency tool( String name )
  {
    return dependency( 'tools', name )
  }

  private Dependency dependency( String group, String name )
  {
    project.dependencies.project( path: ":$group:$name" )
  }
}
