package com.spicter.persistence.testdomain.model;

import com.curtis.model.AssociationEnd;
import com.curtis.model.NamedBusinessObject;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.entity.Aggregated;
import org.apache.polygene.api.property.Property;

public interface Customer extends NamedBusinessObject<Customer>
{
//    @BusinessObjectProperty
    @Optional
    Property<Long> age();

//    @BusinessObjectProperty
    @Optional
    Property<String> email();

//    @BusinessObjectProperty
    @Optional
    Property<Boolean> premium();

    @AssociationEnd
    ManyAssociation<PostalAddress> postalAddresses();

    @AssociationEnd
    @Optional
    Association<ShoppingCart> shoppingCart();

    @AssociationEnd( role = { "customers" } )
    @Optional
    Association<Company> company();

    @AssociationEnd
    @Optional @Aggregated
    Association<Person> person();
}
