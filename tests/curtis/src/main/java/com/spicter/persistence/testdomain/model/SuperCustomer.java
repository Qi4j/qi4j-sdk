package com.spicter.persistence.testdomain.model;

import org.apache.polygene.api.property.Property;

public interface SuperCustomer extends Customer {
    Property<String> extraSkill();
}
