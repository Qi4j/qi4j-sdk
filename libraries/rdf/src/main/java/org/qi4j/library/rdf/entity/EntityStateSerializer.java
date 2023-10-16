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

package org.qi4j.library.rdf.entity;

import java.util.stream.Stream;
import org.eclipse.rdf4j.model.IRI;
import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.serialization.Serializer;
import org.qi4j.api.type.ValueCompositeType;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.util.Classes;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.serialization.JsonSerializer;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.GraphImpl;

/**
 * JAVADOC
 */
public class EntityStateSerializer
{
    @Service
    private JsonSerializer serializer;

    public IRI createEntityIRI(ValueFactory valueFactory, EntityReference reference)
    {
        return valueFactory.createIRI(reference.toURI());
    }

    public Iterable<Statement> serialize(final EntityState entityState)
    {
        return serialize(entityState, true);
    }

    public Iterable<Statement> serialize(final EntityState entityState,
                                         final boolean includeNonQueryable
    )
    {
        Graph graph = new GraphImpl();
        serialize(entityState, includeNonQueryable, graph);
        return graph;
    }

    public void serialize(final EntityState entityState,
                          final boolean includeNonQueryable,
                          final Graph graph
    )
    {
        ValueFactory values = graph.getValueFactory();
        EntityReference reference = entityState.entityReference();
        IRI entityIri = createEntityIRI(values, reference);

        graph.add(entityIri,
            Rdfs.TYPE,
            values.createIRI(Classes.toURI(entityState.entityDescriptor().types().findFirst().orElse(null))));

        serializeProperties(entityState,
            graph,
            entityIri,
            entityState.entityDescriptor(),
            includeNonQueryable);

        serializeAssociations(entityState,
            graph,
            entityIri,
            entityState.entityDescriptor().state().associations(),
            includeNonQueryable);

        serializeManyAssociations(entityState,
            graph,
            entityIri,
            entityState.entityDescriptor().state().manyAssociations(),
            includeNonQueryable);
    }

    private void serializeProperties(EntityState entityState,
                                     Graph graph, Resource subject,
                                     EntityDescriptor entityType,
                                     boolean includeNonQueryable)
    {
        // Properties
        entityType.state().properties().forEach(
            persistentProperty ->
            {
                Object property = entityState.propertyValueOf(persistentProperty.qualifiedName());
                if( property != null )
                {
                    serializeProperty(persistentProperty, property, subject, graph, includeNonQueryable);
                }
            });
    }

    private void serializeProperty(PropertyDescriptor persistentProperty, Object property,
                                   Resource subject, Graph graph,
                                   boolean includeNonQueryable)
    {
        if( !(includeNonQueryable || persistentProperty.queryable()) )
        {
            return; // Skip non-queryable
        }

        ValueType valueType = persistentProperty.valueType();

        final ValueFactory valueFactory = graph.getValueFactory();

        String propertyURI = persistentProperty.qualifiedName().toURI();
        IRI predicate = valueFactory.createIRI(propertyURI);
        String baseURI = propertyURI.substring(0, propertyURI.indexOf('#')) + "/";

        if( valueType instanceof ValueCompositeType )
        {
            serializeValueComposite(subject, predicate, (ValueComposite) property, valueType,
                graph, baseURI, includeNonQueryable);
        }
        else
        {
            String stringProperty = serializer.serialize(Serializer.Options.NO_TYPE_INFO, property);
            final Literal object = valueFactory.createLiteral(stringProperty);
            graph.add(subject, predicate, object);
        }
    }

    private void serializeValueComposite(Resource subject, IRI predicate,
                                         ValueComposite value,
                                         ValueType valueType,
                                         Graph graph,
                                         String baseUri,
                                         boolean includeNonQueryable
    )
    {
        final ValueFactory valueFactory = graph.getValueFactory();
        BNode collection = valueFactory.createBNode();
        graph.add(subject, predicate, collection);

        ((ValueCompositeType) valueType).properties().forEach(
            persistentProperty ->
            {
                Object propertyValue
                    = Qi4jAPI.FUNCTION_COMPOSITE_INSTANCE_OF
                    .apply(value)
                    .state()
                    .propertyFor(persistentProperty.accessor())
                    .get();

                if( propertyValue != null )
                {
                    ValueType type = persistentProperty.valueType();
                    if( type instanceof ValueCompositeType )
                    {
                        IRI pred = valueFactory.createIRI(baseUri, persistentProperty.qualifiedName().name());
                        serializeValueComposite(collection, pred, (ValueComposite) propertyValue,
                            type, graph, baseUri + persistentProperty.qualifiedName().name() + "/",
                            includeNonQueryable);
                    }
                    else
                    {
                        serializeProperty(persistentProperty,propertyValue,collection, graph,includeNonQueryable);
                    }
                }
            });
    }

    private void serializeAssociations(final EntityState entityState,
                                       final Graph graph, IRI entityIri,
                                       final Stream<? extends AssociationDescriptor> associations,
                                       final boolean includeNonQueryable
    )
    {
        ValueFactory values = graph.getValueFactory();

        // Associations
        associations.filter(type -> includeNonQueryable || type.queryable()).forEach(
            associationType ->
            {
                EntityReference associatedId
                    = entityState
                    .associationValueOf(
                        associationType
                            .qualifiedName());
                if( associatedId != null )
                {
                    IRI assocURI = values.createIRI(associationType.qualifiedName().toURI());
                    IRI assocEntityIRI= values.createIRI(associatedId.toURI());
                    graph.add(entityIri,assocURI,assocEntityIRI);
                }
            });
    }

    private void serializeManyAssociations(final EntityState entityState,
                                           final Graph graph,
                                           final IRI entityIri,
                                           final Stream<? extends AssociationDescriptor> associations,
                                           final boolean includeNonQueryable
    )
    {
        ValueFactory values = graph.getValueFactory();

        // Many-Associations
        associations.filter(type -> includeNonQueryable || type.queryable()).forEach(
            associationType ->
            {
                BNode collection = values
                    .createBNode();
                graph.add(entityIri, values.createIRI(associationType.qualifiedName().toURI()), collection);
                graph.add(collection, Rdfs.TYPE, Rdfs.SEQ);

                ManyAssociationState associatedIds = entityState.manyAssociationValueOf(
                    associationType.qualifiedName());
                for( EntityReference associatedId : associatedIds )
                {
                    IRI assocEntityIri = values.createIRI(associatedId.toURI());
                    graph.add(collection, Rdfs.LIST_ITEM, assocEntityIri);
                }
            });
    }
}
