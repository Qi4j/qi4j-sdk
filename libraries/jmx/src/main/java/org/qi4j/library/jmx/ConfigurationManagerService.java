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

package org.qi4j.library.jmx;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.modelmbean.DescriptorSupport;
import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.activation.Activation;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.type.EnumType;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.spi.Qi4jSPI;

/**
 * Expose ConfigurationComposites through JMX.
 * Allow configurations to be edited, and the services to be restarted.
 */
@Mixins( ConfigurationManagerService.Mixin.class )
@Activators( ConfigurationManagerService.Activator.class )
public interface ConfigurationManagerService
{
    void exportConfigurableServices()
            throws Exception;

    void unexportConfigurableServices()
            throws Exception;

    class Activator
            extends ActivatorAdapter<ServiceReference<ConfigurationManagerService>>
    {

        @Override
        public void afterActivation( ServiceReference<ConfigurationManagerService> activated )
                throws Exception
        {
            activated.get().exportConfigurableServices();
        }

        @Override
        public void beforePassivation( ServiceReference<ConfigurationManagerService> passivating )
                throws Exception
        {
            passivating.get().unexportConfigurableServices();
        }

    }

    abstract class Mixin
        implements ConfigurationManagerService
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Service
        MBeanServer server;

        @Structure
        Application application;

        @Structure
        Qi4jSPI spi;

        @Service
        Iterable<ServiceReference<?>> configurableServices;

        private List<ObjectName> configurationNames = new ArrayList<>();

        @Override
        public void exportConfigurableServices()
            throws NotCompliantMBeanException, MBeanRegistrationException, InstanceAlreadyExistsException, MalformedObjectNameException
        {
            for( ServiceReference<?> configurableService : configurableServices )
            {
                Object service = configurableService.get();

                if( !( service instanceof Composite ) )
                {
                    continue; // Skip imported services
                }

                // Check if service has configuration
                CompositeInstance compositeInstance = Qi4jAPI.FUNCTION_COMPOSITE_INSTANCE_OF.apply( (Composite) service );
                try
                {
                    Configuration config = compositeInstance.newProxy( Configuration.class );
                }
                catch( Exception e )
                {
                    // Service does not have configuration
                    continue;
                }

                String serviceClass = compositeInstance.types().findFirst().get().getName();
                ServiceDescriptor serviceDescriptor = spi.serviceDescriptorFor( configurableService );
                ModuleDescriptor module = spi.moduleOf( configurableService );
                Class<Object> configurationClass = serviceDescriptor.configurationType();
                if( configurationClass != null )
                {
                    EntityDescriptor descriptor = module.entityDescriptor( configurationClass.getName() );
                    List<MBeanAttributeInfo> attributes = new ArrayList<>();
                    Map<String, AccessibleObject> properties = new HashMap<>();
                    descriptor.state().properties().forEach( persistentProperty -> {
                        if( !persistentProperty.isImmutable() )
                        {
                            String propertyName = persistentProperty.qualifiedName().name();
                            String type = persistentProperty.valueType().primaryType().getName();

                            Descriptor attrDescriptor = new DescriptorSupport();
                            attrDescriptor.setField( "name", propertyName );
                            attrDescriptor.setField( "descriptorType", "attribute" );

                            if( persistentProperty.valueType() instanceof EnumType )
                            {
                                type = String.class.getName();

                                // Try to add legal values
                                try
                                {
                                    Set<String> legalValues = new LinkedHashSet<>();
                                    Class<?> enumType = getClass().getClassLoader()
                                        .loadClass( persistentProperty.valueType().primaryType().getName() );
                                    for( Field field : enumType.getFields() )
                                    {
                                        legalValues.add( field.getName() );
                                    }
                                    attrDescriptor.setField( "legalValues", legalValues );
                                }
                                catch( ClassNotFoundException e )
                                {
                                    // Ignore
                                    e.printStackTrace();
                                }
                            }
                            attributes.add( new MBeanAttributeInfo( propertyName, type, propertyName, true, true, type.equals( "java.lang.Boolean" ), attrDescriptor ) );
                            properties.put( propertyName, persistentProperty.accessor() );
                        }
                    } );

                    List<MBeanOperationInfo> operations = new ArrayList<>();
                    operations.add( new MBeanOperationInfo( "restart", "Restart service", new MBeanParameterInfo[ 0 ], "java.lang.String", MBeanOperationInfo.ACTION_INFO ) );

                    String mbeanName = configurableService.identity().toString();
                    MBeanInfo mbeanInfo = new MBeanInfo( serviceClass, mbeanName, attributes.toArray( new MBeanAttributeInfo[ attributes
                        .size() ] ), null, operations.toArray( new MBeanOperationInfo[ operations.size() ] ), null );
                    Object mbean = new ConfigurableService( configurableService, mbeanInfo, mbeanName, properties );
                    ObjectName configurableServiceName;
                    ObjectName serviceName = Qi4jMBeans.findServiceName( server, application.name(), mbeanName );
                    if( serviceName != null )
                    {
                        configurableServiceName = new ObjectName( serviceName.toString() + ",name=Configuration" );
                    }
                    else
                    {
                        configurableServiceName = new ObjectName( "Configuration:name=" + mbeanName );
                    }

                    server.registerMBean( mbean, configurableServiceName );
                    configurationNames.add( configurableServiceName );
                }
            }
        }

        @Override
        public void unexportConfigurableServices()
            throws Exception
        {
            for( ObjectName configurableServiceName : configurationNames )
            {
                server.unregisterMBean( configurableServiceName );
            }
        }

        abstract class EditableConfiguration
            implements DynamicMBean
        {
            MBeanInfo info;
            Identity identity;
            Map<String, AccessibleObject> propertyNames;

            EditableConfiguration( MBeanInfo info, String identity, Map<String, AccessibleObject> propertyNames )
            {
                this.info = info;
                this.identity = StringIdentity.identityOf( identity );
                this.propertyNames = propertyNames;
            }

            @Override
            public Object getAttribute( String name )
                throws AttributeNotFoundException, MBeanException, ReflectionException
            {
                UnitOfWork uow = uowf.newUnitOfWork();
                try
                {
                    EntityComposite configuration = uow.get( EntityComposite.class, identity );
                    AssociationStateHolder state = spi.stateOf( configuration );
                    AccessibleObject accessor = propertyNames.get( name );
                    Property<Object> property = state.propertyFor( accessor );
                    Object object = property.get();
                    if( object instanceof Enum )
                    {
                        object = object.toString();
                    }
                    return object;
                }
                catch( Exception ex )
                {
                    throw new ReflectionException( ex, "Could not get attribute " + name );
                }
                finally
                {
                    uow.discard();
                }
            }

            @Override
            public void setAttribute( Attribute attribute )
                throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
            {
                UnitOfWork uow = uowf.newUnitOfWork();
                try
                {
                    EntityComposite configuration = uow.get( EntityComposite.class, identity );
                    AssociationStateHolder state = spi.stateOf( configuration );
                    AccessibleObject accessor = propertyNames.get( attribute.getName() );
                    Property<Object> property = state.propertyFor( accessor );
                    PropertyDescriptor propertyDescriptor = spi.propertyDescriptorFor( property );
                    if( EnumType.isEnum( propertyDescriptor.type() ) )
                    {
                        //noinspection unchecked
                        property.set( Enum.valueOf( (Class<Enum>) propertyDescriptor.type(),
                                                    attribute.getValue().toString() ) );
                    }
                    else
                    {
                        property.set( attribute.getValue() );
                    }

                    try
                    {
                        uow.complete();
                    }
                    catch( UnitOfWorkCompletionException e )
                    {
                        throw new ReflectionException( e );
                    }
                }
                finally
                {
                    uow.discard();
                }
            }

            @Override
            public AttributeList getAttributes( String[] names )
            {
                AttributeList list = new AttributeList();
                for( String name : names )
                {
                    try
                    {
                        Object value = getAttribute( name );
                        list.add( new Attribute( name, value ) );
                    }
                    catch( AttributeNotFoundException | MBeanException | ReflectionException e )
                    {
                        e.printStackTrace();
                    }
                }

                return list;
            }

            @Override
            public AttributeList setAttributes( AttributeList attributeList )
            {
                AttributeList list = new AttributeList();
                for( int i = 0; i < list.size(); i++ )
                {
                    Attribute attribute = (Attribute) list.get( i );

                    try
                    {
                        setAttribute( attribute );
                        list.add( attribute );
                    }
                    catch( AttributeNotFoundException | InvalidAttributeValueException | ReflectionException | MBeanException e )
                    {
                        e.printStackTrace();
                    }
                }

                return list;
            }

            @Override
            public MBeanInfo getMBeanInfo()
            {
                return info;
            }
        }

        class ConfigurableService
            extends EditableConfiguration
        {
            private ServiceReference<?> serviceRef;

            ConfigurableService( ServiceReference<?> serviceReference,
                                 MBeanInfo info,
                                 String identity,
                                 Map<String, AccessibleObject> propertyNames
            )
            {
                super( info, identity, propertyNames );
                this.serviceRef = serviceReference;
            }

            @Override
            public Object invoke( String s, Object[] objects, String[] strings )
                throws MBeanException, ReflectionException
            {
                if( s.equals( "restart" ) )
                {
                    try
                    {
                        // Refresh and restart
                        if( serviceRef.isActive() )
                        {
                            // Refresh configuration
                            CompositeInstance compositeInstance = Qi4jAPI.FUNCTION_COMPOSITE_INSTANCE_OF
                                .apply( (Composite) serviceRef.get() );
                            compositeInstance.newProxy( Configuration.class ).refresh();

                            ( (Activation) serviceRef ).passivate();
                            ( (Activation) serviceRef ).activate();
                        }

                        return "Service restarted";
                    }
                    catch( Exception e )
                    {
                        return "Could not restart service:" + e.getMessage();
                    }
                }

                return "Unknown operation";
            }
        }
    }
}
