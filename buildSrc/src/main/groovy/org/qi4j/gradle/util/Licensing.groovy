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

class Licensing
{
  static String withLicenseHeader( String base, String flavour )
  {
    def header
    switch( flavour )
    {
      case 'java': case 'groovy': case 'js':
        header = licenseHeader_wrap( base, '/*', ' * ', ' */' ); break
      case 'xml': case 'html':
        header = licenseHeader_wrap( base, '<!--', '  ', '-->' ); break
      case 'txt': case 'shell': case 'python': case 'ruby':
        header = licenseHeader_wrap( base, null, '# ', null ); break
      case 'adoc': case 'asciidoc':
        header = licenseHeader_wrap( base, null, '// ', null ); break
      default:
        header = base
    }
    header
  }

  private static String licenseHeader_wrap( String base, String top, String left, String bottom )
  {
    ( top ? "$top\n" : '' ) +
    base.readLines().collect { "${ left }${ it }" }.join( '\n' ) + '\n' +
    ( bottom ? "$bottom\n" : '' )
  }

  private Licensing() {}
}
