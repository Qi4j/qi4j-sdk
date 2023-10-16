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

import java.lang.reflect.Method;
import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.qi4j.api.util.Classes;
import org.qi4j.library.rdf.Rdfs;

public final class SerializerContext
{
    private final Graph graph;
    private final ValueFactory valueFactory;

    public SerializerContext( Graph graph )
    {
        this.valueFactory = graph.getValueFactory();
        this.graph = graph;
    }

    public Graph getGraph()
    {
        return graph;
    }

    public ValueFactory getValueFactory()
    {
        return valueFactory;
    }

    public String createServiceUri( String layer, String module, Class type, String identity )
    {
        String serviceType = Classes.normalizeClassToURI( type.getName() );
        String moduleUri = createModuleUri( layer, module );
        return moduleUri + "/" + serviceType + "/" + identity;
    }

    public String createCompositeUri( String module, Class composite )
    {
        String compositeName = Classes.normalizeClassToURI( composite.getName() );
        return module + "/" + compositeName;
    }

    public String createApplicationUri( String app )
    {
        return "urn:qi4j:model:" + app;
    }

    public String createLayerUri( String appUri, String layer )
    {
        return appUri + "/" + layer;
    }

    public String createModuleUri( String layerUri, String module )
    {
        return layerUri + "/" + module;
    }

    public void setNameAndType( String node, String name, IRI type )
    {
        addType( node, type );
        addName( node, name );
    }


    public void addName( String subject, String name )
    {
        Value nameValue = valueFactory.createLiteral( name );
        IRI subjectUri = valueFactory.createIRI( subject );
        graph.add( valueFactory.createStatement( subjectUri, Rdfs.LABEL, nameValue ) );
    }

    public void addType( String subject, IRI type )
    {
        IRI subjectUri = valueFactory.createIRI( subject );
        Statement statement = valueFactory.createStatement( subjectUri, Rdfs.TYPE, type );
        graph.add( statement );
    }

    public void addStatement( String subject, IRI predicate, String literal )
    {
        Literal object = valueFactory.createLiteral( literal );
        IRI subjectUri = valueFactory.createIRI( subject );
        Statement statement = valueFactory.createStatement( subjectUri, predicate, object );
        graph.add( statement );
    }

    public void addRelationship( String subject, IRI relationship, String object )
    {
        IRI subjectUri = valueFactory.createIRI( subject );
        IRI objectUri = valueFactory.createIRI( object );
        Statement statement = valueFactory.createStatement( subjectUri, relationship, objectUri );
        graph.add( statement );
    }

    public void addStatement( String subject, IRI predicate, boolean literal )
    {
        IRI subjectUri = valueFactory.createIRI( subject );
        Literal object = valueFactory.createLiteral( literal );
        Statement statement = valueFactory.createStatement( subjectUri, predicate, object );
        graph.add( statement );
    }

    public String createCompositeMethodUri( String compositeUri, Method method )
    {
        return compositeUri + "/" + method.toGenericString().replace( " ", "_" );
    }
}
