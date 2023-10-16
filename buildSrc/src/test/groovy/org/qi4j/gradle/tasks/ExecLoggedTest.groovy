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
package org.qi4j.gradle.tasks

import org.gradle.internal.os.OperatingSystem
import org.gradle.process.ExecSpec
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification

@IgnoreIf( { OperatingSystem.current().isWindows() } )
class ExecLoggedTest extends Specification
{
  @Rule
  TemporaryFolder tmpDir = new TemporaryFolder()

  File script

  def setup()
  {
    script = tmpDir.newFile( 'script.sh' ) << '''
      #!/bin/sh
      echo STDOUT
      echo STDERR 1>&2
    '''.stripIndent().trim()
  }

  def "ExecLogged.execLogged()"()
  {
    given:
    def project = ProjectBuilder.builder().build()
    def out = tmpDir.newFile 'out.txt'
    def err = tmpDir.newFile 'err.txt'

    when:
    ExecLogged.execLogged project, out, err, { ExecSpec spec ->
      spec.workingDir = tmpDir.root
      spec.commandLine 'sh', script.absolutePath
    }

    then:
    out.text == 'STDOUT\n'
    err.text == 'STDERR\n'
  }

  def "ExecLogged Task"()
  {
    given:
    def project = ProjectBuilder.builder().build()
    def out = tmpDir.newFile 'out.txt'
    def err = tmpDir.newFile 'err.txt'
    def task = project.tasks.create( 'test', ExecLogged ) { ExecLogged task ->
      task.workingDir = tmpDir.root
      task.stdoutFile = out
      task.stderrFile = err
      task.commandLine 'sh', script.absolutePath
    }

    when:
    // WARN ProjectBuilder is not meant to run tasks, should use TestKit instead
    // But that's enough for testing ExecLogged and much more faster
    task.actions.each { it.execute(task) }

    then:
    out.text == 'STDOUT\n'
    err.text == 'STDERR\n'
  }
}
