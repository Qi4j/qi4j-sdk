package com.spicter.persistence.testdomain.model;

import com.curtis.model.AssociationEnd;
import com.curtis.model.NamedBusinessObject;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.common.Optional;

public interface Person extends NamedBusinessObject<Person>
{
    @AssociationEnd
    @Optional
    Association<Customer> customer();
}
