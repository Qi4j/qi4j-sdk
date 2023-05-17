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

package org.qi4j.index.rdf.query;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.QuerySpecification;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.openrdf.query.QueryLanguage;

/**
 * JAVADOC Add JavaDoc
 */
@Mixins( { RdfQueryService.RdfEntityFinderMixin.class } )
public interface RdfQueryService
    extends EntityFinder, RdfQueryParserFactory
{
    /**
     * JAVADOC Add JavaDoc
     */
    class RdfEntityFinderMixin
        implements EntityFinder
    {

        private static final QueryLanguage language = QueryLanguage.SPARQL;

        @Service
        private RdfQueryParserFactory queryParserFactory;

        @This
        TupleQueryExecutor tupleExecutor;

        @Override
        public Stream<EntityReference> findEntities( Class<?> resultType,
                                                     Predicate<Composite> whereClause,
                                                     List<OrderBy> orderBySegments,
                                                     Integer firstResult,
                                                     Integer maxResults,
                                                     Map<String, Object> variables ) throws EntityFinderException
        {
            CollectingQualifiedIdentityResultCallback collectingCallback = new CollectingQualifiedIdentityResultCallback();

            if( QuerySpecification.isQueryLanguage( "SERQL", whereClause ))
            {
                String query = ((QuerySpecification)whereClause).query();
                tupleExecutor.performTupleQuery( QueryLanguage.SERQL, query, variables, collectingCallback );
                return collectingCallback.entities().stream();

            } else
            {
                RdfQueryParser rdfQueryParser = queryParserFactory.newQueryParser( language );
                String query = rdfQueryParser.constructQuery( resultType, whereClause, orderBySegments, firstResult, maxResults, variables );

                tupleExecutor.performTupleQuery( language, query, variables, collectingCallback );
                return collectingCallback.entities().stream();
            }
        }

        @Override
        public EntityReference findEntity( Class<?> resultType, Predicate<Composite> whereClause, Map<String, Object> variables )
            throws EntityFinderException
        {
            final SingleQualifiedIdentityResultCallback singleCallback = new SingleQualifiedIdentityResultCallback();

            if (QuerySpecification.isQueryLanguage( "SERQL", whereClause))
            {
                String query = ((QuerySpecification)whereClause).query();
                tupleExecutor.performTupleQuery( QueryLanguage.SERQL, query, variables, singleCallback );
                return singleCallback.qualifiedIdentity();
            } else
            {
                RdfQueryParser rdfQueryParser = queryParserFactory.newQueryParser( language );
                String query = rdfQueryParser.constructQuery( resultType, whereClause, null, null, null, variables );
                tupleExecutor.performTupleQuery( QueryLanguage.SPARQL, query, variables, singleCallback );
                return singleCallback.qualifiedIdentity();
            }
        }

        @Override
        public long countEntities( Class<?> resultType, Predicate<Composite> whereClause, Map<String, Object> variables )
            throws EntityFinderException
        {
            if (QuerySpecification.isQueryLanguage( "SERQL", whereClause ))
            {
                String query = ((QuerySpecification)whereClause).query();
                return tupleExecutor.performTupleQuery( QueryLanguage.SERQL, query, variables, null );

            } else
            {
                RdfQueryParser rdfQueryParser = queryParserFactory.newQueryParser( language );
                String query = rdfQueryParser.constructQuery( resultType, whereClause, null, null, null, variables );
                return tupleExecutor.performTupleQuery( language, query, variables, null );
            }
        }
    }
}
