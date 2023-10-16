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

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class SettingsPlugin implements Plugin<Settings>
{
  @Override
  void apply( Settings settings )
  {
    checkPreconditions settings
  }

  static void checkPreconditions( Settings setting )
  {
    def java = JavaVersion.current()
    def minimum = JavaVersion.VERSION_14
    if( java < minimum )
    {
      throw new Exception( "Cannot build using Java ${ java }, please use ${ JavaVersion.VERSION_14 } or greater." )
    }
  }
}
