package com.curtis.model;

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.property.Property;

public interface NamedBusinessObject<T> extends BusinessObject
{
    @Optional
    Property<String> name();
}
