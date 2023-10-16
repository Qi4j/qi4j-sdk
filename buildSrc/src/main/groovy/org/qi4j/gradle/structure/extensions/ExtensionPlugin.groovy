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
package org.qi4j.gradle.structure.extensions

import org.qi4j.gradle.code.PublishedCodePlugin
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

import static org.qi4j.gradle.structure.ProjectGroupTasks.configureProjectGroupTasks

@CompileStatic
class ExtensionPlugin implements Plugin<Project>
{
  @Override
  void apply( Project project )
  {
    project.plugins.apply PublishedCodePlugin
    configureProjectGroupTasks( "extensions", project )
  }
}
