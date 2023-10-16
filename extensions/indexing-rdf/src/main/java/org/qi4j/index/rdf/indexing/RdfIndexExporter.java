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
 *
 *
 */

package org.qi4j.index.rdf.indexing;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.spi.query.IndexExporter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

/**
 * JAVADOC
 */
@Mixins( RdfIndexExporter.RdfExporterMixin.class )
public interface RdfIndexExporter extends IndexExporter
{
    /**
     * JAVADOC
     */
    class RdfExporterMixin
        implements IndexExporter
    {
        @Service
        private Repository repository;

        @Override
        public void exportReadableToStream( PrintStream out )
            throws IOException
        {
            RDFWriter rdfWriter = Rio.createWriter( RDFFormat.TRIG, out );
            exportToWriter( rdfWriter );
        }

        @Override
        public void exportFormalToWriter( PrintWriter out )
            throws IOException
        {
            RDFWriter rdfWriter = Rio.createWriter( RDFFormat.RDFXML, out );
            exportToWriter( rdfWriter );
        }

        private void exportToWriter( RDFWriter rdfWriter )
            throws IOException
        {
            try
            {
                final RepositoryConnection connection = repository.getConnection();
                try
                {
                    connection.export( rdfWriter );
                }
                catch( Exception e )
                {
                    e.printStackTrace();
                    throw new IOException( "Problem writing to RdfWriter", e );
                }
                finally
                {
                    connection.close();
                }
            }
            catch( RepositoryException e )
            {
                throw new IOException( e );
            }
        }
    }
}
