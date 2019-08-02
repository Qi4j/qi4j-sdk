package com.spicter.persistence.testdomain.model;

import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.QueryBuilder;
import org.apache.polygene.api.query.QueryBuilderFactory;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.unitofwork.concern.UnitOfWorkPropagation;

import static org.apache.polygene.api.query.QueryExpressions.eq;
import static org.apache.polygene.api.query.QueryExpressions.templateFor;

@Mixins(CustomerQueries.Mixin.class)
public interface CustomerQueries // extends Queries<Customer>
{

    Query<Customer> findByEmail( String email );

    Query<Customer> findByName( String name );

    Query<Customer> findAll();

    class Mixin
        implements CustomerQueries
    {
        @Structure
        private QueryBuilderFactory qbf;

        @Structure
        private UnitOfWorkFactory uowf;

        @Override
        @UnitOfWorkPropagation
        public Query<Customer> findByEmail( String email )
        {
            QueryBuilder<Customer> qb = qbf.newQueryBuilder( Customer.class );
            Customer customer = templateFor( Customer.class );
            qb = qb.where( eq( customer.email(), email ) );
            return uowf.currentUnitOfWork().newQuery( qb );
        }

        @Override
        @UnitOfWorkPropagation
        public Query<Customer> findByName( String name )
        {
            QueryBuilder<Customer> qb = qbf.newQueryBuilder( Customer.class );
            Customer customer = templateFor( Customer.class );
            qb = qb.where( eq( customer.name(), name ) );
            return uowf.currentUnitOfWork().newQuery( qb );
        }

        @Override
        @UnitOfWorkPropagation
        public Query<Customer> findAll()
        {
            QueryBuilder<Customer> qb = qbf.newQueryBuilder( Customer.class );
            return uowf.currentUnitOfWork().newQuery( qb );
        }
    }
}
