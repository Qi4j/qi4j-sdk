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
package org.qi4j.gradle.code

class PublishNaming
{
  static String publishedNameFor( String projectPath )
  {
    def toName = { List<String> path -> path.drop( 1 ).join( '.' ) }
    def path = projectPath.split( ':' ).drop( 1 ) as List<String>
    def newPath
    if( path[ 0 ] == 'libraries' )
    {
      newPath = "org.qi4j.library.${ toName( path ) }"
    }
    else if( path[ 0 ].endsWith( 's' ) )
    {
      newPath = "org.qi4j.${ path[ 0 ].substring( 0, path[ 0 ].length() - 1 ) }.${ toName( path ) }"
    }
    else if( path.size() > 1 )
    {
      newPath = "org.qi4j.${ path[ 0 ] }.${ toName( path ) }"
    }
    else
    {
      newPath = "org.qi4j.${ path[ 0 ] }"
    }
    return newPath
  }
}
