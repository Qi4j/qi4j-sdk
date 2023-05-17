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
package org.qi4j.gradle.structure.release

import groovy.transform.CompileStatic
import org.qi4j.gradle.TaskGroups
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

@CompileStatic
class ReleaseSpecPlugin implements Plugin<Project>
{
  static class TaskNames
  {
    static final String RELEASE_APPROVED_PROJECTS = 'releaseSpecApprovedProjects'
    static final String REPORT_RELEASE_SPEC = 'reportReleaseSpec'
    static final String CHECK_RELEASE_SPEC = 'checkReleaseSpec'
  }

  @Override
  void apply( final Project project )
  {
    applyReleaseSpecExtension project
    if( project == project.rootProject )
    {
      configureReleaseSpecTasks project
    }
  }

  private static void applyReleaseSpecExtension( Project project )
  {
    project.extensions.create( ReleaseSpecExtension.NAME, ReleaseSpecExtension, project.rootProject )
  }

  private static void configureReleaseSpecTasks( Project project )
  {
    project.tasks.create( TaskNames.RELEASE_APPROVED_PROJECTS, ReleaseApprovedProjectsTask ) { Task task ->
      task.group = TaskGroups.RELEASE
    }
    project.tasks.create( TaskNames.REPORT_RELEASE_SPEC, ReleaseSpecReportTask ) { Task task ->
      task.group = TaskGroups.RELEASE
    }
    project.tasks.create( TaskNames.CHECK_RELEASE_SPEC, CheckReleaseSpecTask ) { Task task ->
      task.group = TaskGroups.RELEASE_VERIFICATION
    }
  }
}
