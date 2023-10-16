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

package org.qi4j.library.rdf.serializer;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RDFWriterFactory;
import org.qi4j.library.rdf.Qi4jRdf;
import org.qi4j.library.rdf.Rdfs;

abstract class AbstractSerializer
    implements Serializer
{
    private Class<? extends RDFWriterFactory> writerFactoryClass;

    protected AbstractSerializer( Class<? extends RDFWriterFactory> writerFactoryClass )
    {
        this.writerFactoryClass = writerFactoryClass;
    }

    @Override
    public void serialize( Iterable<Statement> graph, Writer out ) throws RDFHandlerException
    {
        String[] prefixes = { "qi4j", "rdf", "rdfs" };
        String[] namespaces = { Qi4jRdf.QI4J_MODEL, Rdfs.RDF, Rdfs.RDFS };
        serialize( graph, out, prefixes, namespaces );
    }

    @Override
    public void serialize( Iterable<Statement> graph, Writer out, String[] namespacePrefixes, String[] namespaces )
        throws RDFHandlerException
    {
        RDFWriterFactory writerFactory;
        try
        {
            writerFactory = writerFactoryClass.getConstructor().newInstance();
        }
        catch( IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e )
        {
            throw new InternalError();
        }
        RDFWriter writer = writerFactory.getWriter( out );
        writer.startRDF();
        for( int i = 0; i < namespacePrefixes.length; i++ )
        {
            String namespacePrefix = namespacePrefixes[ i ];
            String namespace = namespaces[ i ];
            writer.handleNamespace( namespacePrefix, namespace );
        }
        for( Statement st : graph )
        {
            writer.handleStatement( st );
        }
        writer.endRDF();
    }

}