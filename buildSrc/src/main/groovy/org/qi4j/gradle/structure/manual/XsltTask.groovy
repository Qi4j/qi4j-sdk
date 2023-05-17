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
package org.qi4j.gradle.structure.manual

import groovy.transform.CompileStatic
import org.gradle.api.file.EmptyFileVisitor
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.file.FileVisitDetails
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 *  Gradle task for running a set of one or more
 *  files through an XSLT transform.  A styleSheet
 *  file must be specified.  The source file(s) are
 *  configured just like any other source task:
 *     source <file>
 *       ...or...
 *     source <directory>
 *       ...and then optionally...
 *     include '*.xml'
 *     exclude, etc.
 *
 *  One of destDir or destFile must be specified, though if
 *  there are multiple source files then destFile will just
 *  keep getting rewritten.
 *
 *  The extension is stripped from the source files and the
 *  specified extension is appended (whether it is set or not)
 *  it defaults to no extension.
 *
 *  Example task formatting a check style report:
 *
 *  task checkstyleReport(type: XsltTask, dependsOn: check) {
 *      source project.checkstyleResultsDir
 *      include '*.xml'
 *
 *      destDir = project.checkstyleResultsDir
 *      extension = 'html'
 *
 *      stylesheetFile = file( 'config/checkstyle/checkstyle-noframes.xsl' )
 * }
 *
 *  The above definition requires that the specified XSL file be
 *  copied in with the other checkstyle configuration files.  (The
 *  file in the example is part of the checkstyle distribution.)
 *
 */
@CompileStatic
class XsltTask extends SourceTask
{

  @OutputDirectory
  File destDir

  @Input
  String extension = 'html'

  @InputFile
  File stylesheetFile

  @TaskAction
  def transform()
  {
    def factory = TransformerFactory.newInstance()
    def transformer = factory.newTransformer( new StreamSource( stylesheetFile ) );

    getSource().visit( new EmptyFileVisitor() {
      @Override
      void visitFile( FileVisitDetails fvd )
      {
        // Remove the extension from the file name
        def name = fvd.file.name.replaceAll( '[.][^\\.]*$', '' )
        name += '.' + extension
        def destFile = new File( destDir, name )
        transformer.transform( new StreamSource( fvd.file ), new StreamResult( destFile ) )
      }
    } )
  }
}
