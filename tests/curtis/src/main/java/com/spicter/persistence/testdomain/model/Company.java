package com.spicter.persistence.testdomain.model;

import com.curtis.model.AssociationEnd;
import com.curtis.model.NamedBusinessObject;
import org.apache.polygene.api.association.ManyAssociation;

public interface Company extends NamedBusinessObject<Company>
{
    @AssociationEnd( role ={ "company"})
    ManyAssociation<Customer> customers();
}
