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
package org.qi4j.gradle.util

class Environment
{
  static boolean isExecutableInPath( String executable )
  {
    def pathDirs = System.getenv( 'PATH' ).split( File.pathSeparator )
    def flattened = pathDirs.collect( { String pathDir -> new File( pathDir, 'executable' ) } ).flatten() as List<File>
    return flattened.find( { File pathDir -> pathDir.isFile() } ) != null
  }

  private Environment() {}
}
