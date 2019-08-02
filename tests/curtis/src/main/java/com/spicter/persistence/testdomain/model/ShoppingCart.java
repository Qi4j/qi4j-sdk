package com.spicter.persistence.testdomain.model;

import com.curtis.model.AssociationEnd;
import com.curtis.model.NamedBusinessObject;
import org.apache.polygene.api.association.ManyAssociation;

public interface ShoppingCart extends NamedBusinessObject<ShoppingCart>
{
    @AssociationEnd
    ManyAssociation<ShoppingItem> shoppingItems();


    @AssociationEnd(role = "shoppingCart")
    ManyAssociation<Customer> customer();
}
