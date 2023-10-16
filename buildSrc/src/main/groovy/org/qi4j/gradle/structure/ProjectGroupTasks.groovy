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
package org.qi4j.gradle.structure

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Task

@CompileStatic
class ProjectGroupTasks
{
  static void configureProjectGroupTasks( String projectGroup, Project project )
  {
    project.tasks.create( "check${ projectGroup.capitalize() }" ) { Task task ->
//      task.group = TaskGroups.VERIFICATION
      task.description = "Runs the $projectGroup checks"
      task.dependsOn( project.tasks.getByName( "check" ) )
    }
  }
}
