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
package org.qi4j.api;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.function.Function;
import org.qi4j.api.association.AbstractAssociation;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.value.ValueDescriptor;

/**
 * Encapsulation of the Qi4j API.
 */
public interface Qi4jAPI
{
    /**
     * If a Modifier gets a reference to the Composite using @This,
     * then that reference must be dereferenced using this method
     * before handing it out for others to use.
     *
     * @param <T>       Parameterized type of the Composite
     * @param composite instance reference injected in Modified using @This
     *
     * @return the dereferenced Composite
     */
    <T> T dereference( T composite );

    /**
     * Returns the Module where the Composite, ServiceReference or UnitOfWork belongs.
     *
     * @param compositeOrServiceReferenceOrUow The Composite (Service, Value, Entity or Transient) or ServiceReference
     *                                         or UnitOfWork to lookup the Module it belongs to
     *
     * @return The Module instance where the Composite, ServiceReference or UnitOfWork belongs to
     */
    ModuleDescriptor moduleOf(Object compositeOrServiceReferenceOrUow );

    /**
     * Returns the ModelDescriptor of the Composite or ServiceReference.
     *
     * @param compositeOrServiceReference The Composite (Service, Value, Entity or Transient) of ServiceReference
     *                                    for which to lookup the ModelDescriptor
     *
     * @return The ModelDescriptor of the Composite or ServiceReference
     */
    ModelDescriptor modelDescriptorFor(Object compositeOrServiceReference );

    /**
     * Returns the CompositeDescriptor of the Composite or ServiceReference.
     *
     * @param compositeOrServiceReference The Composite (Service, Value, Entity or Transient)  or ServiceReference
     *                                    for which to lookup the CompositeDescriptor
     *
     * @return The CompositeDescriptor of the Composite or ServiceReference
     */
    CompositeDescriptor compositeDescriptorFor(Object compositeOrServiceReference );

    /**
     * Returns the TransientDescriptor of the TransientComposite.
     *
     * @param transsient The TransientComposite for which to lookup the TransientDescriptor
     *
     * @return The TransientDescriptor of the TransientComposite
     */
    TransientDescriptor transientDescriptorFor(Object transsient );

    /**
     * Returns the EntityDescriptor of the EntityComposite.
     *
     * @param entity The EntityComposite for which to lookup the EntityDescriptor
     *
     * @return The EntityDescriptor of the EntityComposite
     */
    EntityDescriptor entityDescriptorFor(Object entity );

    /**
     * Returns the ValueDescriptor of the ValueComposite.
     *
     * @param value The ValueComposite for which to lookup the ValueDescriptor
     *
     * @return The ValueDescriptor of the ValueComposite
     */
    ValueDescriptor valueDescriptorFor(Object value );

    /**
     * Returns the ServiceDescriptor of the ServiceComposite.
     *
     * @param service The ServiceComposite for which to lookup the ServiceDescriptor
     *
     * @return The ServiceDescriptor of the ServiceComposite
     */
    ServiceDescriptor serviceDescriptorFor(Object service );

    /**
     * Returns the PropertyDescriptor of the Property.
     *
     * @param property The Property for which to lookup the PropertyDescriptor
     *
     * @return The PropertyDescriptor of the Property
     */
    PropertyDescriptor propertyDescriptorFor(Property<?> property );

    /**
     * Returns the AssociationDescriptor of the Association.
     *
     * @param association The Association for which to lookup the AssociationDescriptor
     *
     * @return The AssociationDescriptor of the Association
     */
    AssociationDescriptor associationDescriptorFor(AbstractAssociation association );

    /** Tests if the given object is a Composite
     *
     * @param object The Java object to be checked
     * @return true if the given object is a Composite type.
     */
    boolean isComposite( Object object );

    /**
     * Function that returns the CompositeInstance of a Composite.
     */
    Function<Composite, CompositeInstance> FUNCTION_COMPOSITE_INSTANCE_OF = composite -> {
        if( composite instanceof Proxy )
        {
            return ( (CompositeInstance) Proxy.getInvocationHandler( composite ) );
        }
        try
        {
            Class<? extends Composite> compositeClass = composite.getClass();
            Field instanceField = compositeClass.getField( "_instance" );
            Object instance = instanceField.get( composite );
            return (CompositeInstance) instance;
        }
        catch( Exception e )
        {
            throw new InternalError( "Could not get _instance field. This field should have been automatically generated by the Qi4j runtime.", e );
        }
    };

    /**
     * Function that returns the CompositeDescriptor of a Composite.
     */
    Function<Composite, CompositeDescriptor> FUNCTION_DESCRIPTOR_FOR = FUNCTION_COMPOSITE_INSTANCE_OF.andThen( CompositeInstance::descriptor );
}
