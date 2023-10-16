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
package org.qi4j.gradle.structure.manual

import org.qi4j.gradle.code.PublishNaming
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class AsciidocBuildInfoPlugin implements Plugin<Project>
{
  final static String TASK_NAME = 'makeAsciidocBuildInfo'

  void apply( Project project )
  {
    def buildInfoDir = new File( project.buildDir, "docs/buildinfo" );

    def task = project.tasks.create( TASK_NAME )
//    task.group = TaskGroups.DOCUMENTATION
    task.description = 'Generates asciidoc artifact snippet'
    task.doLast {
      buildInfoDir.mkdirs()

      // GroupID, ArtifactID, Version table in artifact.txt
      def artifactTableFile = new File( buildInfoDir, "artifact.txt" )
      def artifactTable = """
        |.Artifact
        |[role="artifact", options="header,autowidth"]
        ||===================================================
        ||Group ID|Artifact ID|Version
        ||${ project.group }|${ PublishNaming.publishedNameFor( project.path ) }|${ project.version }
        ||===================================================
        """.stripMargin()
      artifactTableFile.withWriter { out -> out.println( artifactTable ) }
    }

    // Declare inputs/outputs
    task.inputs.property( 'groupId', project.group )
    task.inputs.property( 'artifactId', PublishNaming.publishedNameFor( project.path ) )
    task.inputs.property( 'version', project.version )
    task.outputs.file( buildInfoDir )
  }
}
