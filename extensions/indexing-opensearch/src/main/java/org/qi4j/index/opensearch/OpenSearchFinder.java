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
package org.qi4j.index.opensearch;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;
import org.opensearch.client.util.ObjectBuilder;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.grammar.AndPredicate;
import org.qi4j.api.query.grammar.AssociationNotNullPredicate;
import org.qi4j.api.query.grammar.AssociationNullPredicate;
import org.qi4j.api.query.grammar.BinaryPredicate;
import org.qi4j.api.query.grammar.ComparisonPredicate;
import org.qi4j.api.query.grammar.ContainsAllPredicate;
import org.qi4j.api.query.grammar.ContainsPredicate;
import org.qi4j.api.query.grammar.EqPredicate;
import org.qi4j.api.query.grammar.GePredicate;
import org.qi4j.api.query.grammar.GtPredicate;
import org.qi4j.api.query.grammar.LePredicate;
import org.qi4j.api.query.grammar.LtPredicate;
import org.qi4j.api.query.grammar.ManyAssociationContainsPredicate;
import org.qi4j.api.query.grammar.MatchesPredicate;
import org.qi4j.api.query.grammar.NamedAssociationContainsNamePredicate;
import org.qi4j.api.query.grammar.NamedAssociationContainsPredicate;
import org.qi4j.api.query.grammar.NePredicate;
import org.qi4j.api.query.grammar.Notpredicate;
import org.qi4j.api.query.grammar.OrPredicate;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.query.grammar.PropertyNotNullPredicate;
import org.qi4j.api.query.grammar.PropertyNullPredicate;
import org.qi4j.api.query.grammar.QuerySpecification;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.index.opensearch.OpenSearchFinderSupport.ComplexTypeSupport;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.index.query.BoolQuery.Builder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.opensearch.client.opensearch._types.query_dsl.QueryBuilders.*;
import static org.qi4j.index.opensearch.OpenSearchFinderSupport.resolveVariable;

@Mixins( OpenSearchFinder.Mixin.class )
public interface OpenSearchFinder
    extends EntityFinder
{
    class Mixin
        implements EntityFinder
    {
        private static final Logger LOGGER = LoggerFactory.getLogger( OpenSearchFinder.class );
        private static final Map<Class<?>, ComplexTypeSupport> COMPLEX_TYPE_SUPPORTS = new HashMap<>( 0 );

        @This
        private OpenSearchSupport support;

        @Override
        public Stream<EntityReference> findEntities( Class<?> resultType,
                                                     Predicate<Composite> whereClause,
                                                     List<OrderBy> orderBySegments,
                                                     Integer firstResult,
                                                     Integer maxResults,
                                                     Map<String, Object> variables ) throws EntityFinderException
        {
            // Prepare request
            SearchRequestBuilder request = support.client().prepareSearch( support.index() );

            BoolQuery.Builder baseQueryBuilder = baseQuery( resultType );
            QueryBuilder whereQueryBuilder = processWhereSpecification( baseQueryBuilder, whereClause, variables );

            request.setQuery( QueryBuilders.bool().must( whereQueryBuilder ).filter( baseQueryBuilder ) );
            if( firstResult != null )
            {
                request.setFrom( firstResult );
            }
            if( maxResults != null )
            {
                request.setSize( maxResults );
            }
            else
            {
                //request.setSize( Integer.MAX_VALUE ); // TODO Use scrolls?
            }
            if( orderBySegments != null )
            {
                for( OrderBy order : orderBySegments )
                {
                    request.addSort( order.property().toString(),
                                     order.order() == OrderBy.Order.ASCENDING ? SortOrder.ASC : SortOrder.DESC );
                }
            }

            // Log
            LOGGER.debug( "Will search Entities: {}", request );

            // Execute
            SearchResponse response = request.execute().actionGet();

            return StreamSupport.stream( response.getHits().spliterator(), false )
                                .map( hit -> EntityReference.parseEntityReference( hit.getId() ) );
        }

        @Override
        public EntityReference findEntity( Class<?> resultType,
                                           Predicate<Composite> whereClause,
                                           Map<String, Object> variables )
            throws EntityFinderException
        {
            // Prepare request
            SearchRequestBuilder request = support.client().prepareSearch( support.index() );

            BoolQuery.Builder baseQueryBuilder = baseQuery( resultType );
            QueryBuilder whereQueryBuilder = processWhereSpecification( baseQueryBuilder, whereClause, variables );

            request.setQuery( QueryBuilders.bool().must( whereQueryBuilder ).filter( baseQueryBuilder ) );
            request.setSize( 1 );

            // Log
            LOGGER.debug( "Will search Entity: {}", request );

            // Execute
            SearchResponse response = request.execute().actionGet();

            if( response.getHits().getTotalHits() == 1 )
            {
                return EntityReference.parseEntityReference( response.getHits().getAt( 0 ).getId() );
            }

            return null;
        }

        @Override
        public long countEntities( Class<?> resultType,
                                   Predicate<Composite> whereClause,
                                   Map<String, Object> variables )
            throws EntityFinderException
        {
            // Prepare request
            SearchRequestBuilder request = support.client().prepareSearch( support.index() ).setSize( 0 );

            BoolQuery.Builder baseQueryBuilder = baseQuery( resultType );
            QueryBuilder whereQueryBuilder = processWhereSpecification( baseQueryBuilder, whereClause, variables );

            request.setQuery( QueryBuilders.bool().must( whereQueryBuilder ).filter( baseQueryBuilder ) );

            // Log
            LOGGER.debug( "Will count Entities: {}", request );

            // Execute
            SearchResponse count = request.execute().actionGet();

            return count.getHits().getTotalHits();
        }

        private static BoolQuery.Builder baseQuery( Class<?> resultType )
        {
            return QueryBuilders.bool().must( builder -> {

                TermQuery.Builder types = term().field("_types").value(new FieldValue.Builder().stringValue(resultType.getName()).build());
                return types;
                } 
            );
        }

        private ObjectBuilder<BoolQuery> processWhereSpecification(BoolQuery.Builder queryBuilder,
                                                        Predicate<Composite> spec,
                                                        Map<String, Object> variables )
            throws EntityFinderException
        {
            if( spec == null )
            {
                return matchAllQuery();
            }

            if( spec instanceof QuerySpecification )
            {
                return wrapperQuery( ( (QuerySpecification) spec ).query() );
            }

            processSpecification( queryBuilder, spec, variables );
            return matchAllQuery();
        }

        private void processSpecification( BoolQuery.Builder queryBuilder,
                                           Predicate<Composite> spec,
                                           Map<String, Object> variables )
            throws EntityFinderException
        {
            if( spec instanceof BinaryPredicate )
            {
                BinaryPredicate binSpec = (BinaryPredicate) spec;
                processBinarySpecification( queryBuilder, binSpec, variables );
            }
            else if( spec instanceof Notpredicate )
            {
                Notpredicate notSpec = (Notpredicate) spec;
                processNotSpecification( queryBuilder, notSpec, variables );
            }
            else if( spec instanceof ComparisonPredicate )
            {
                ComparisonPredicate<?> compSpec = (ComparisonPredicate<?>) spec;
                processComparisonSpecification( queryBuilder, compSpec, variables );
            }
            else if( spec instanceof ContainsAllPredicate )
            {
                ContainsAllPredicate<?> contAllSpec = (ContainsAllPredicate) spec;
                processContainsAllSpecification( queryBuilder, contAllSpec, variables );
            }
            else if( spec instanceof ContainsPredicate )
            {
                ContainsPredicate<?> contSpec = (ContainsPredicate) spec;
                processContainsSpecification( queryBuilder, contSpec, variables );
            }
            else if( spec instanceof MatchesPredicate )
            {
                MatchesPredicate matchSpec = (MatchesPredicate) spec;
                processMatchesSpecification( queryBuilder, matchSpec, variables );
            }
            else if( spec instanceof PropertyNotNullPredicate )
            {
                PropertyNotNullPredicate<?> propNotNullSpec = (PropertyNotNullPredicate) spec;
                processPropertyNotNullSpecification( queryBuilder, propNotNullSpec );
            }
            else if( spec instanceof PropertyNullPredicate )
            {
                PropertyNullPredicate<?> propNullSpec = (PropertyNullPredicate) spec;
                processPropertyNullSpecification( queryBuilder, propNullSpec );
            }
            else if( spec instanceof AssociationNotNullPredicate )
            {
                AssociationNotNullPredicate<?> assNotNullSpec = (AssociationNotNullPredicate) spec;
                processAssociationNotNullSpecification( queryBuilder, assNotNullSpec );
            }
            else if( spec instanceof AssociationNullPredicate )
            {
                AssociationNullPredicate<?> assNullSpec = (AssociationNullPredicate) spec;
                processAssociationNullSpecification( queryBuilder, assNullSpec );
            }
            else if( spec instanceof ManyAssociationContainsPredicate )
            {
                ManyAssociationContainsPredicate<?> manyAssContSpec = (ManyAssociationContainsPredicate) spec;
                processManyAssociationContainsSpecification( queryBuilder, manyAssContSpec, variables );
            }
            else if( spec instanceof NamedAssociationContainsPredicate )
            {

                NamedAssociationContainsPredicate<?> namedAssContSpec = (NamedAssociationContainsPredicate) spec;
                processNamedAssociationContainsSpecification( queryBuilder, namedAssContSpec, variables );
            }
            else if( spec instanceof NamedAssociationContainsNamePredicate )
            {

                NamedAssociationContainsNamePredicate<?> namedAssContNameSpec
                    = (NamedAssociationContainsNamePredicate) spec;
                processNamedAssociationContainsNameSpecification( queryBuilder, namedAssContNameSpec, variables );
            }
            else
            {
                throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search "
                                                         + "(New Query API support missing?): "
                                                         + spec.getClass() + ": " + spec );
            }
        }

        private void processBinarySpecification( BoolQuery.Builder queryBuilder,
                                                 BinaryPredicate spec,
                                                 Map<String, Object> variables )
            throws EntityFinderException
        {
            LOGGER.trace( "Processing BinarySpecification {}", spec );
            Iterable<Predicate<Composite>> operands = spec.operands();

            if( spec instanceof AndPredicate )
            {
                BoolQuery.Builder andBuilder = QueryBuilders.bool();
                for( Predicate<Composite> operand : operands )
                {
                    processSpecification( andBuilder, operand, variables );
                }
                queryBuilder.must( andBuilder );
            }
            else if( spec instanceof OrPredicate )
            {
                BoolQuery.Builder orBuilder = QueryBuilders.bool();
                for( Predicate<Composite> operand : operands )
                {
                    BoolQuery.Builder shouldBuilder = QueryBuilders.bool();
                    processSpecification( shouldBuilder, operand, variables );
                    orBuilder.should( shouldBuilder );
                }
                orBuilder.minimumShouldMatch( 1 );
                queryBuilder.must( orBuilder );
            }
            else
            {
                throw new UnsupportedOperationException( "Binary Query specification is nor an AndSpecification "
                                                         + "nor an OrSpecification, cannot continue." );
            }
        }

        private void processNotSpecification( BoolQuery.Builder queryBuilder,
                                              Notpredicate spec,
                                              Map<String, Object> variables )
            throws EntityFinderException
        {
            LOGGER.trace( "Processing NotSpecification {}", spec );
            BoolQuery.Builder operandBuilder = QueryBuilders.bool();
            processSpecification( operandBuilder, spec.operand(), variables );
            queryBuilder.mustNot( operandBuilder );
        }

        private void processComparisonSpecification( BoolQuery.Builder queryBuilder,
                                                     ComparisonPredicate<?> spec,
                                                     Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ComparisonSpecification {}", spec );

            if( spec.value() instanceof ValueComposite )
            {
                // Query by "example value"
                throw new UnsupportedOperationException( "ElasticSearch Index/Query does not support complex "
                                                         + "queries, ie. queries by 'example value'." );
            }
            else if( COMPLEX_TYPE_SUPPORTS.get( spec.value().getClass() ) != null )
            {
                // Query on complex type property
                ComplexTypeSupport support = COMPLEX_TYPE_SUPPORTS.get( spec.value().getClass() );
                queryBuilder.must( support.comparison( spec, variables ) );
            }
            else
            {
                // Query by simple property value
                String name = spec.property().toString();
                Object value = resolveVariable( spec.value(), variables );
                if( spec instanceof EqPredicate )
                {
                    queryBuilder.must( termQuery( name, value ) );
                }
                else if( spec instanceof NePredicate )
                {
                    queryBuilder.must( existsQuery( name ) ).mustNot( termQuery( name, value ) );
                }
                else if( spec instanceof GePredicate )
                {
                    queryBuilder.must( rangeQuery( name ).gte( value ) );
                }
                else if( spec instanceof GtPredicate )
                {
                    queryBuilder.must( rangeQuery( name ).gt( value ) );
                }
                else if( spec instanceof LePredicate )
                {
                    queryBuilder.must( rangeQuery( name ).lte( value ) );
                }
                else if( spec instanceof LtPredicate )
                {
                    queryBuilder.must( rangeQuery( name ).lt( value ) );
                }
                else
                {
                    throw new UnsupportedOperationException( "Query specification unsupported by Elastic Search "
                                                             + "(New Query API support missing?): "
                                                             + spec.getClass() + ": " + spec );
                }
            }
        }

        private void processContainsAllSpecification( BoolQuery.Builder queryBuilder,
                                                      ContainsAllPredicate<?> spec,
                                                      Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ContainsAllSpecification {}", spec );
            Collection<?> values = spec.containedValues();
            if( values.isEmpty() )
            {
                // Ignore empty contains all spec
                return;
            }
            Object firstValue = values.iterator().next();
            if( firstValue instanceof ValueComposite )
            {
                // Query by complex property "example value"
                throw new UnsupportedOperationException( "ElasticSearch Index/Query does not support complex "
                                                         + "queries, ie. queries by 'example value'." );
            }
            else if( COMPLEX_TYPE_SUPPORTS.get( firstValue.getClass() ) != null )
            {
                ComplexTypeSupport support = COMPLEX_TYPE_SUPPORTS.get( firstValue.getClass() );
                queryBuilder.must( support.containsAll( spec, variables ) );
            }
            else
            {
                String name = spec.collectionProperty().toString();
                BoolQuery.Builder contAllBuilder = QueryBuilders.bool();
                for( Object value : values )
                {
                    contAllBuilder.must( termQuery( name, resolveVariable( value, variables ) ) );
                }
                queryBuilder.must( contAllBuilder );
            }
        }

        private void processContainsSpecification( BoolQuery.Builder queryBuilder,
                                                   ContainsPredicate<?> spec,
                                                   Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ContainsSpecification {}", spec );
            String name = spec.collectionProperty().toString();
            if( spec.value() instanceof ValueComposite )
            {
                // Query by complex property "example value"
                throw new UnsupportedOperationException( "ElasticSearch Index/Query does not support complex "
                                                         + "queries, ie. queries by 'example value'." );
            }
            else if( COMPLEX_TYPE_SUPPORTS.get( spec.value().getClass() ) != null )
            {
                ComplexTypeSupport support = COMPLEX_TYPE_SUPPORTS.get( spec.value().getClass() );
                queryBuilder.must( support.contains( spec, variables ) );
            }
            else
            {
                Object value = resolveVariable( spec.value(), variables );
                queryBuilder.must( termQuery( name, value ) );
            }
        }

        private void processMatchesSpecification( BoolQuery.Builder queryBuilder,
                                                  MatchesPredicate spec,
                                                  Map<String, Object> variables )
        {
            LOGGER.trace( "Processing MatchesSpecification {}", spec );
            String name = spec.property().toString();
            String regexp = resolveVariable( spec.regexp(), variables ).toString();
            queryBuilder.must( regexpQuery( name, regexp ) );
        }

        private void processPropertyNotNullSpecification( BoolQuery.Builder queryBuilder,
                                                          PropertyNotNullPredicate<?> spec )
        {
            LOGGER.trace( "Processing PropertyNotNullSpecification {}", spec );
            queryBuilder.must( existsQuery( spec.property().toString() ) );
        }

        private void processPropertyNullSpecification( BoolQuery.Builder queryBuilder,
                                                       PropertyNullPredicate<?> spec )
        {
            LOGGER.trace( "Processing PropertyNullSpecification {}", spec );
            queryBuilder.mustNot( existsQuery( ( spec.property().toString() ) ) );
        }

        private void processAssociationNotNullSpecification( BoolQuery.Builder queryBuilder,
                                                             AssociationNotNullPredicate<?> spec )
        {
            LOGGER.trace( "Processing AssociationNotNullSpecification {}", spec );
            queryBuilder.must( existsQuery( spec.association().toString() + ".identity" ) );
        }

        private void processAssociationNullSpecification( BoolQuery.Builder queryBuilder,
                                                          AssociationNullPredicate<?> spec )
        {
            LOGGER.trace( "Processing AssociationNullSpecification {}", spec );
            queryBuilder.mustNot( existsQuery( ( spec.association().toString() + ".identity" ) ) );
        }

        private void processManyAssociationContainsSpecification( BoolQuery.Builder queryBuilder,
                                                                  ManyAssociationContainsPredicate<?> spec,
                                                                  Map<String, Object> variables )
        {
            LOGGER.trace( "Processing ManyAssociationContainsSpecification {}", spec );
            String name = spec.manyAssociation().toString() + ".identity";
            Object value = resolveVariable( spec.value(), variables );
            queryBuilder.must( termQuery( name, value ) );
        }

        private void processNamedAssociationContainsSpecification( BoolQuery.Builder queryBuilder,
                                                                   NamedAssociationContainsPredicate<?> spec,
                                                                   Map<String, Object> variables )
        {
            LOGGER.trace( "Processing NamedAssociationContainsSpecification {}", spec );
            String name = spec.namedAssociation().toString() + ".identity";
            Object value = resolveVariable( spec.value(), variables );
            queryBuilder.must( termQuery( name, value ) );
        }

        private void processNamedAssociationContainsNameSpecification( BoolQuery.Builder queryBuilder,
                                                                       NamedAssociationContainsNamePredicate<?> spec,
                                                                       Map<String, Object> variables )
        {
            LOGGER.trace( "Processing NamedAssociationContainsNameSpecification {}", spec );
            String name = spec.namedAssociation().toString() + "._named";
            Object value = resolveVariable( spec.name(), variables );
            queryBuilder.must( termQuery( name, value ) );
        }
    }
}
