package com.spicter.persistence.testdomain.model;

public interface FindCustomerByItem // extends QueryBuilder<Customer, FindCustomerByItem>
{

//    @Has( "shoppingCart.name = :shoppingCartName" )
    FindCustomerByItem shoppingCartName( String shoppingCartName );

//    @Has( "item.name LIKE :namePattern" )
    FindCustomerByItem itemNamePattern( String namePattern );
}
