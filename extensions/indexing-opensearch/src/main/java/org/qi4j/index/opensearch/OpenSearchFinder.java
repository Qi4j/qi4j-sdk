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

import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.QueryBuilders;
import org.opensearch.client.opensearch.core.CountRequest;
import org.opensearch.client.opensearch.core.CountResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;

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
import org.qi4j.api.value.ValueComposite;
import org.qi4j.index.opensearch.OpenSearchFinderSupport.ComplexTypeSupport;
import org.qi4j.spi.query.EntityFinder;
import org.qi4j.spi.query.EntityFinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qi4j.index.opensearch.OpenSearchFinderSupport.resolveFieldValue;
import static org.qi4j.index.opensearch.OpenSearchFinderSupport.resolveVariable;

@Mixins(OpenSearchFinder.Mixin.class)
public interface OpenSearchFinder extends EntityFinder {
    class Mixin implements EntityFinder {
        private static final Logger LOGGER = LoggerFactory.getLogger(OpenSearchFinder.class);
        private static final Map<Class<?>, ComplexTypeSupport> COMPLEX_TYPE_SUPPORTS = new HashMap<>(0);

        @This
        private OpenSearchSupport support;

        @Override
        public Stream<EntityReference> findEntities(Class<?> resultType,
                                                    Predicate<Composite> whereClause,
                                                    List<OrderBy> orderBySegments,
                                                    Integer firstResult,
                                                    Integer maxResults,
                                                    Map<String, Object> variables) throws EntityFinderException {
            try {
                OpenSearchClient client = support.client();

                Query typeFilter = typeFilter(resultType);
                BoolQuery.Builder whereBool = QueryBuilders.bool();
                Query whereQuery = buildWhere(whereBool, whereClause, variables);

                Query finalQuery = mergeWhereAndType(whereBool, whereQuery, typeFilter);

                SearchRequest.Builder req = new SearchRequest.Builder()
                    .index(support.index())
                    .query(finalQuery);

                if (firstResult != null) {
                    req.from(firstResult);
                }
                if (maxResults != null) {
                    req.size(maxResults);
                }

                if (orderBySegments != null) {
                    for (OrderBy order : orderBySegments) {
                        String field = order.property().toString();
                        SortOrder dir = order.order() == OrderBy.Order.ASCENDING ? SortOrder.Asc : SortOrder.Desc;
                        req.sort(SortOptions.of(s -> s.field(f -> f.field(field).order(dir))));
                    }
                }

                SearchRequest request = req.build();
                LOGGER.debug("Will search Entities: {}", request);
                SearchResponse<Void> response = client.search(request, Void.class);

                return response.hits().hits().stream()
                               .map(hit -> EntityReference.parseEntityReference(hit.id()));

            } catch (Exception e) {
                throw new EntityFinderException("OpenSearch findEntities failed", e);
            }
        }

        @Override
        public EntityReference findEntity(Class<?> resultType,
                                          Predicate<Composite> whereClause,
                                          Map<String, Object> variables) throws EntityFinderException {
            try {
                OpenSearchClient client = support.client();

                Query typeFilter = typeFilter(resultType);
                BoolQuery.Builder whereBool = QueryBuilders.bool();
                Query whereQuery = buildWhere(whereBool, whereClause, variables);

                Query finalQuery = mergeWhereAndType(whereBool, whereQuery, typeFilter);

                SearchRequest request = new SearchRequest.Builder()
                    .index(support.index())
                    .size(1)
                    .query(finalQuery)
                    .build();

                LOGGER.debug("Will search Entity: {}", request);

                SearchResponse<Void> response = client.search(request, Void.class);

                if (response.hits().total() != null && response.hits().total().value() == 1L) {
                    return EntityReference.parseEntityReference(response.hits().hits().getFirst().id());
                }
                return null;
            } catch (Exception e) {
                throw new EntityFinderException("OpenSearch findEntity failed", e);
            }
        }

        @Override
        public long countEntities(Class<?> resultType,
                                  Predicate<Composite> whereClause,
                                  Map<String, Object> variables) throws EntityFinderException {
            try {
                OpenSearchClient client = support.client();

                Query typeFilter = typeFilter(resultType);
                BoolQuery.Builder whereBool = QueryBuilders.bool();
                Query whereQuery = buildWhere(whereBool, whereClause, variables);

                Query finalQuery = mergeWhereAndType(whereBool, whereQuery, typeFilter);

                CountRequest countReq = new CountRequest.Builder()
                    .index(support.index())
                    .query(finalQuery)
                    .build();

                LOGGER.debug("Will count Entities: {}", countReq);

                CountResponse count = client.count(countReq);
                return count.count();
            } catch (Exception e) {
                throw new EntityFinderException("OpenSearch countEntities failed", e);
            }
        }

        private Query typeFilter(Class<?> resultType) {
            return QueryBuilders.term()
                .field("_types")
                .value(FieldValue.of(resultType.getName()))
                .build()
                .toQuery();
        }

        private Query buildWhere(BoolQuery.Builder whereBool,
                                 Predicate<Composite> spec,
                                 Map<String, Object> variables) throws EntityFinderException {
            if (spec == null) {
                return QueryBuilders.matchAll().build().toQuery();
            }
            processSpecification(whereBool, spec, variables);
            return null;
        }

        private Query mergeWhereAndType(BoolQuery.Builder whereBool, Query whereQuery, Query typeFilter) {
            BoolQuery.Builder b = QueryBuilders.bool();
            if (whereQuery != null) {
                b.must(whereQuery);
            }
            b.filter(typeFilter);

            BoolQuery built = whereBool.build();
            if (!built.must().isEmpty()) {
                b.must(built.must());
            }
            if (!built.should().isEmpty()) {
                b.should(built.should());
                if (built.minimumShouldMatch() != null) {
                    b.minimumShouldMatch(built.minimumShouldMatch());
                }
            }
            if (!built.mustNot().isEmpty()) {
                b.mustNot(built.mustNot());
            }
            return b.build().toQuery();
        }

        @SuppressWarnings("rawtypes")
        private void processSpecification(BoolQuery.Builder queryBuilder,
                                          Predicate<Composite> spec,
                                          Map<String, Object> variables) throws EntityFinderException {
            switch(spec)
            {
                case BinaryPredicate binSpec -> processBinarySpecification(queryBuilder, binSpec, variables);
                case Notpredicate notSpec -> processNotSpecification(queryBuilder, notSpec, variables);
                case ComparisonPredicate compSpec -> processComparisonSpecification(queryBuilder, compSpec, variables);
                case ContainsAllPredicate contAllSpec -> processContainsAllSpecification(queryBuilder, contAllSpec, variables);
                case ContainsPredicate contSpec -> processContainsSpecification(queryBuilder, contSpec, variables);
                case MatchesPredicate matchSpec -> processMatchesSpecification(queryBuilder, matchSpec, variables);
                case PropertyNotNullPredicate propNotNullSpec -> processPropertyNotNullSpecification(queryBuilder, propNotNullSpec);
                case PropertyNullPredicate propNullSpec -> processPropertyNullSpecification(queryBuilder, propNullSpec);
                case AssociationNotNullPredicate assNotNullSpec -> processAssociationNotNullSpecification(queryBuilder, assNotNullSpec);
                case AssociationNullPredicate assNullSpec -> processAssociationNullSpecification(queryBuilder, assNullSpec);
                case ManyAssociationContainsPredicate manyAssContSpec -> processManyAssociationContainsSpecification(queryBuilder, manyAssContSpec, variables);
                case NamedAssociationContainsPredicate namedAssContSpec -> processNamedAssociationContainsSpecification(queryBuilder, namedAssContSpec, variables);
                case NamedAssociationContainsNamePredicate namedAssContNameSpec -> processNamedAssociationContainsNameSpecification(queryBuilder, namedAssContNameSpec, variables);
                default -> throw new UnsupportedOperationException(
                    "Query specification unsupported by OpenSearch (New Query API support missing?): "
                        + spec.getClass() + ": " + spec);
            }
        }

        private void processBinarySpecification(BoolQuery.Builder queryBuilder,
                                                BinaryPredicate spec,
                                                Map<String, Object> variables) throws EntityFinderException {
            LOGGER.trace("Processing BinarySpecification {}", spec);
            Iterable<Predicate<Composite>> operands = spec.operands();

            if (spec instanceof AndPredicate) {
                BoolQuery.Builder andBuilder = QueryBuilders.bool();
                for (Predicate<Composite> operand : operands) {
                    processSpecification(andBuilder, operand, variables);
                }
                BoolQuery built = andBuilder.build();
                BoolQuery.Builder inner = QueryBuilders.bool()
                    .must(built.must())
                    .should(built.should())
                    .mustNot(built.mustNot());
                queryBuilder.must(inner.build().toQuery());
            } else if (spec instanceof OrPredicate) {
                BoolQuery.Builder orBuilder = QueryBuilders.bool();
                for (Predicate<Composite> operand : operands) {
                    BoolQuery.Builder shouldBuilder = QueryBuilders.bool();
                    processSpecification(shouldBuilder, operand, variables);
                    BoolQuery sb = shouldBuilder.build();
                    BoolQuery.Builder inner = QueryBuilders.bool()
                        .must(sb.must())
                        .should(sb.should())
                        .mustNot(sb.mustNot());
                    orBuilder.should(inner.build().toQuery());
                }
                orBuilder.minimumShouldMatch("1");
                BoolQuery ob = orBuilder.build();
                BoolQuery.Builder wrap = QueryBuilders.bool()
                    .should(ob.should())
                    .minimumShouldMatch("1");
                queryBuilder.must(wrap.build().toQuery());
            } else {
                throw new UnsupportedOperationException(
                    "Binary Query specification is neither And nor Or, cannot continue.");
            }
        }

        private void processNotSpecification(BoolQuery.Builder queryBuilder,
                                             Notpredicate spec,
                                             Map<String, Object> variables) throws EntityFinderException {
            LOGGER.trace("Processing NotSpecification {}", spec);
            BoolQuery.Builder operandBuilder = QueryBuilders.bool();
            processSpecification(operandBuilder, spec.operand(), variables);
            BoolQuery ob = operandBuilder.build();
            BoolQuery.Builder inner = QueryBuilders.bool()
                .must(ob.must())
                .should(ob.should())
                .mustNot(ob.mustNot());
            queryBuilder.mustNot(inner.build().toQuery());
        }

        @SuppressWarnings("rawtypes")
        private void processComparisonSpecification(BoolQuery.Builder queryBuilder,
                                                    ComparisonPredicate<?> spec,
                                                    Map<String, Object> variables) {
            LOGGER.trace("Processing ComparisonSpecification {}", spec);

            if (spec.value() instanceof ValueComposite) {
                throw new UnsupportedOperationException(
                    "OpenSearch Index/Query does not support complex queries, ie. queries by 'example value'.");
            } else if (COMPLEX_TYPE_SUPPORTS.get(spec.value().getClass()) != null) {
                ComplexTypeSupport cts = COMPLEX_TYPE_SUPPORTS.get(spec.value().getClass());
                queryBuilder.must(cts.comparison(spec, variables).build());
            } else {
                String name = spec.property().toString();
                Object value = resolveVariable(spec.value(), variables);
                switch(spec)
                {
                    case EqPredicate eqPredicate -> queryBuilder.must(QueryBuilders.term().field(name).value(resolveFieldValue(value)).build().toQuery());
                    case NePredicate nePredicate ->
                    {
                        queryBuilder.must(QueryBuilders.exists().field(name).build().toQuery());
                        FieldValue fieldValue = resolveFieldValue(value);
                        queryBuilder.mustNot(QueryBuilders.term().field(name).value(fieldValue).build().toQuery());
                    }
                    case GePredicate gePredicate -> queryBuilder.must(QueryBuilders.range().field(name).gte(JsonData.of(value)).build().toQuery());
                    case GtPredicate gtPredicate -> queryBuilder.must(QueryBuilders.range().field(name).gt(JsonData.of(value)).build().toQuery());
                    case LePredicate lePredicate -> queryBuilder.must(QueryBuilders.range().field(name).lte(JsonData.of(value)).build().toQuery());
                    case LtPredicate ltPredicate -> queryBuilder.must(QueryBuilders.range().field(name).lt(JsonData.of(value)).build().toQuery());
                    default -> throw new UnsupportedOperationException(
                        "Query specification unsupported by OpenSearch (New Query API support missing?): "
                            + spec.getClass() + ": " + spec);
                }
            }
        }

        private void processContainsAllSpecification(BoolQuery.Builder queryBuilder,
                                                     ContainsAllPredicate<?> spec,
                                                     Map<String, Object> variables) {
            LOGGER.trace("Processing ContainsAllSpecification {}", spec);
            Collection<?> values = spec.containedValues();
            if (values.isEmpty()) {
                return;
            }
            Object first = values.iterator().next();
            if (first instanceof ValueComposite) {
                throw new UnsupportedOperationException(
                    "OpenSearch Index/Query does not support complex queries, ie. queries by 'example value'.");
            } else if (COMPLEX_TYPE_SUPPORTS.get(first.getClass()) != null) {
                ComplexTypeSupport cts = COMPLEX_TYPE_SUPPORTS.get(first.getClass());
                queryBuilder.must(cts.containsAll(spec, variables).build());
            } else {
                String name = spec.collectionProperty().toString();
                BoolQuery.Builder contAll = QueryBuilders.bool();
                for (Object v : values) {
                    Object resolved = resolveVariable(v, variables);
                    contAll.must(QueryBuilders.term().field(name).value(resolveFieldValue(resolved)).build().toQuery());
                }
                BoolQuery cb = contAll.build();
                if (!cb.must().isEmpty()) {
                    queryBuilder.must(QueryBuilders.bool().must(cb.must()).build().toQuery());
                }
            }
        }

        private void processContainsSpecification(BoolQuery.Builder queryBuilder,
                                                  ContainsPredicate<?> spec,
                                                  Map<String, Object> variables) {
            LOGGER.trace("Processing ContainsSpecification {}", spec);
            String name = spec.collectionProperty().toString();
            if (spec.value() instanceof ValueComposite) {
                throw new UnsupportedOperationException(
                    "OpenSearch Index/Query does not support complex queries, ie. queries by 'example value'.");
            } else if (COMPLEX_TYPE_SUPPORTS.get(spec.value().getClass()) != null) {
                ComplexTypeSupport cts = COMPLEX_TYPE_SUPPORTS.get(spec.value().getClass());
                queryBuilder.must(cts.contains(spec, variables).build());
            } else {
                Object value = resolveVariable(spec.value(), variables);
                queryBuilder.must(QueryBuilders.term().field(name).value(resolveFieldValue(value)).build().toQuery());
            }
        }

        private void processMatchesSpecification(BoolQuery.Builder queryBuilder,
                                                 MatchesPredicate spec,
                                                 Map<String, Object> variables) {
            LOGGER.trace("Processing MatchesSpecification {}", spec);
            String name = spec.property().toString();
            String regexp = resolveVariable(spec.regexp(), variables).toString();
            queryBuilder.must(QueryBuilders.regexp().field(name).value(regexp).build().toQuery());
        }

        private void processPropertyNotNullSpecification(BoolQuery.Builder queryBuilder,
                                                         PropertyNotNullPredicate<?> spec) {
            LOGGER.trace("Processing PropertyNotNullSpecification {}", spec);
            queryBuilder.must(QueryBuilders.exists().field(spec.property().toString()).build().toQuery());
        }

        private void processPropertyNullSpecification(BoolQuery.Builder queryBuilder,
                                                      PropertyNullPredicate<?> spec) {
            LOGGER.trace("Processing PropertyNullSpecification {}", spec);
            queryBuilder.mustNot(QueryBuilders.exists().field(spec.property().toString()).build().toQuery());
        }

        private void processAssociationNotNullSpecification(BoolQuery.Builder queryBuilder,
                                                            AssociationNotNullPredicate<?> spec) {
            LOGGER.trace("Processing AssociationNotNullSpecification {}", spec);
            queryBuilder.must(QueryBuilders.exists().field(spec.association().toString() + ".identity").build().toQuery());
        }

        private void processAssociationNullSpecification(BoolQuery.Builder queryBuilder,
                                                         AssociationNullPredicate<?> spec) {
            LOGGER.trace("Processing AssociationNullSpecification {}", spec);
            queryBuilder.mustNot(QueryBuilders.exists().field(spec.association().toString() + ".identity").build().toQuery());
        }

        private void processManyAssociationContainsSpecification(BoolQuery.Builder queryBuilder,
                                                                 ManyAssociationContainsPredicate<?> spec,
                                                                 Map<String, Object> variables) {
            LOGGER.trace("Processing ManyAssociationContainsSpecification {}", spec);
            String name = spec.manyAssociation().toString() + ".identity";
            Object value = resolveVariable(spec.value(), variables);
            queryBuilder.must(QueryBuilders.term().field(name).value(resolveFieldValue(value.toString())).build().toQuery());
        }

        private void processNamedAssociationContainsSpecification(BoolQuery.Builder queryBuilder,
                                                                  NamedAssociationContainsPredicate<?> spec,
                                                                  Map<String, Object> variables) {
            LOGGER.trace("Processing NamedAssociationContainsSpecification {}", spec);
            String name = spec.namedAssociation().toString() + ".identity";
            Object value = resolveVariable(spec.value(), variables);
            queryBuilder.must(QueryBuilders.term().field(name).value(resolveFieldValue(value.toString())).build().toQuery());
        }

        private void processNamedAssociationContainsNameSpecification(BoolQuery.Builder queryBuilder,
                                                                      NamedAssociationContainsNamePredicate<?> spec,
                                                                      Map<String, Object> variables) {
            LOGGER.trace("Processing NamedAssociationContainsNameSpecification {}", spec);
            String name = spec.namedAssociation().toString() + "._named";
            Object value = resolveVariable(spec.name(), variables);
            queryBuilder.must(QueryBuilders.term().field(name).value(resolveFieldValue(value)).build().toQuery());
        }
    }
}
