package com.spicter.persistence.testdomain.model;

import com.curtis.model.AssociationEnd;
import com.curtis.model.BusinessObject;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.property.Property;

public interface PostalAddress extends BusinessObject<PostalAddress>
{
    @Optional
    Property<String> city();

    @Optional
    Property<String> street();

    @Optional
    Property<String> houseNumber();

    @Optional
    Property<String> nameOnAddress();

    @Optional
    Property<Country> country();

    // creates a bi-directional parent-child link which we want to work.
    @AssociationEnd
    ManyAssociation<Customer> residents();

}
