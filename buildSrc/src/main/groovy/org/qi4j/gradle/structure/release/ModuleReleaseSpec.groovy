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

import org.gradle.api.Project

class ModuleReleaseSpec
{
  static boolean satisfiedBy(Project project )
  {
    def devStatusFile = new File( project.projectDir, "dev-status.xml" )
    if( !devStatusFile.exists() )
    {
      return false
    }
    def module = new groovy.xml.XmlSlurper().parse( devStatusFile )
    def codebase = module.status.codebase.text() as String
    def docs = module.status.documentation.text() as String
    def tests = module.status.unittests.text() as String
    return satisfiedBy( codebase, docs, tests )
  }

  static boolean satisfiedBy(String codebase, String docs, String tests )
  {
    def satisfied = ( codebase == 'none' && docs == 'complete' )
    satisfied |= ( codebase == 'early'
            && ( docs == 'complete' || docs == 'good' )
            && ( tests == 'complete' || tests == 'good' ) )
    satisfied |= ( codebase == 'beta'
            && ( docs == 'complete' || docs == 'good' || docs == 'brief' )
            && ( tests == 'complete' || tests == 'good' || tests == 'some' ) )
    satisfied |= ( codebase == 'stable' )
    satisfied |= ( codebase == 'mature' )
    return satisfied
  }
}
