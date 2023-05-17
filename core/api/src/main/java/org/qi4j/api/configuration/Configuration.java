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

package org.qi4j.api.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;
import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.PropertyMapper;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.identity.HasIdentity;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.serialization.Deserializer;
import org.qi4j.api.serialization.Serialization;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.qualifier.ServiceTags;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.NoSuchEntityTypeException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Provide Configurations for Services. A Service that wants to be configurable
 * should inject a reference to Configuration with the Configuration type:
 * <pre><code>
 *  * &#64;This Configuration&#60;MyServiceConfiguration&#62; config;
 * </code></pre>
 * <p>
 * where MyServiceConfiguration extends {@link ConfigurationComposite}, which itself is an ordinary
 * {@link EntityComposite}. The Configuration implementation
 * will either locate an instance of the given Configuration type in the
 * persistent store using the reference of the Service, or create a new such instance
 * if one doesn't already exist.
 * </p>
 * <p>
 * If a new Configuration instance is created then it will be populated with properties
 * from the properties file whose filesystem name is the same as the reference (e.g. "MyService.properties").
 * If a service is not given a name via the {@code org.qi4j.bootstrap.ServiceDeclaration#identifiedBy(String)}, the
 * name will default to the FQCN of the ServiceComposite type.
 * </p>
 * <p>
 * The Configuration instance can be modified externally just like any other EntityComposite, but
 * its values will not be updated in the Service until {@link #refresh()} is called. This allows
 * safe reloads of Configuration state to ensure that it is not reloaded while the Service is handling
 * a request.
 * </p>
 * <p>
 * The Configuration will be automatically refreshed when the Service is activated by the Qi4j runtime.
 * Any refreshes at other points will have to be done manually or triggered through some other
 * mechanism.
 * </p>
 * <p>
 * The user configuration entity is part of a long running {@link UnitOfWork}, and to persist changes to it the
 * {@link #save()} method must be called. No other actions are required. Example;
 * </p>
 * <pre><code>
 *
 * public interface MyConfiguration extends ConfigurationComposite
 * {
 *     Property&lt;Long&gt; timeout();
 * }
 *
 * :
 *
 * &#64;This Configuration&lt;MyConfiguration&gt; config;
 * :
 * private void setTimeoutConfiguration( long timeout )
 * {
 *     config.get().timeout().set( timeout );
 *     config.save();
 * }
 * </code></pre>
 * <p>
 * And even if a separate thread is using the {@code timeout()} configuration when this is happening, the
 * {@link UnitOfWork} isolation will ensure that the other thread is not affected. That thread, on the other hand
 * will need to do a {@link #refresh()} at an appropriate time to pick up the timeout change. For instance;
 * </p>
 * <pre><code>
 *
 * &#64;Service InventoryService remoteInventoryService;
 *
 * public void restockInventoryItem( InventoryItemId id, int itemCount )
 * {
 *     config.refresh();
 *     long timeout = config.get().timeout().get();
 *
 *     remoteInventoryService.restock( id, itemCount, timeout );
 *
 *     :
 *     :
 * }
 * </code></pre>
 *
 * @param <T> Configuration type
 */
@SuppressWarnings( "JavadocReference" )
@Mixins( Configuration.ConfigurationMixin.class )
public interface Configuration<T>
{
    /**
     * Retrieves the user configuration instance managed by this Configuration.
     * <p>
     * Even if the user configuration is initialized from properties file, the consistency rules of Qi4j composites
     * still applies. If the the properties file is missing a value, then the initialization will fail with a
     * RuntimeException. If Constraints has been defined, those will need to be satisfied as well. The user
     * configuration instance returned will fulfill the constraints and consistency normal to all composites, and
     * can therefor safely be used with additional checks.
     * </p>
     *
     * @return The fully initialized and ready-to-use user configuration instance.
     */
    T get();

    /**
     * Updates the values of the managed user ConfigurationComposite instance from the underlying
     * {@code org.qi4j.spi.entitystore.EntityStore}.  Any modified values in the current user configuration that
     * has not been saved, via {@link #save()} method, will be lost.
     */
    void refresh();

    /**
     * Persists the modified values in the user configuration instance to the underlying store.
     */
    void save();

    /**
     * Implementation of Configuration.
     * <p>
     * This is effectively an internal class in Qi4j and should never be used directly by user code.
     * </p>
     *
     * @param <T> Configuration type
     */
    class ConfigurationMixin<T>
        implements Configuration<T>
    {
        private T configuration;
        private UnitOfWork uow;

        @Structure
        private Qi4jAPI api;

        @This
        private ServiceComposite me;

        @Structure
        private UnitOfWorkFactory uowf;

        @Structure
        private Module module;

        public ConfigurationMixin()
        {
        }

        @Override
        public synchronized T get()
        {
            if( configuration == null )
            {
                Usecase usecase = UsecaseBuilder.newUsecase( "Configuration:" + me.identity().get() );
                uow = uowf.newUnitOfWork( usecase );
                try
                {
                    ServiceDescriptor serviceModel = api.serviceDescriptorFor( me );
                    Identity identity = me.identity().get();
                    configuration = this.findConfigurationInstanceFor( serviceModel, identity, uow );
                }
                catch( InstantiationException e )
                {
                    throw new IllegalStateException( e );
                }
            }

            return configuration;
        }

        @Override
        public synchronized void refresh()
        {
            if( configuration != null )
            {
                configuration = null;
                uow.discard();
                uow = null;
            }
        }

        @Override
        public void save()
        {
            if( uow != null )
            {
                try
                {
                    uow.complete();
                    uow = null;
                }
                catch( UnitOfWorkCompletionException e )
                {
                    // Should be impossible
                    e.printStackTrace();
                }

                configuration = null; // Force refresh
            }
        }

        @SuppressWarnings( "unchecked" )
        public <V> V findConfigurationInstanceFor( ServiceDescriptor serviceModel, Identity serviceIdentity, UnitOfWork uow )
            throws InstantiationException
        {
            Class<V> configurationType = serviceModel.configurationType();
            V configuration;
            try
            {
                configuration = uow.get( configurationType, serviceIdentity );
                uow.pause();
            }
            catch( NoSuchEntityException | NoSuchEntityTypeException e )
            {
                EntityDescriptor entityDescriptor = module.typeLookup().lookupEntityModel( configurationType );
                if( entityDescriptor == null )
                {
                    throw new NoSuchConfigurationTypeException( configurationType, module.descriptor() );
                }
                return (V) initializeConfigurationInstance( entityDescriptor, uow, serviceModel, serviceIdentity );
            }
            return configuration;
        }

        @SuppressWarnings( "unchecked" )
        private <V extends HasIdentity> V initializeConfigurationInstance( EntityDescriptor entityDescriptor,
                                                                           UnitOfWork uow,
                                                                           ServiceDescriptor serviceModel,
                                                                           Identity identity
                                                                         )
            throws InstantiationException
        {
            Usecase usecase = UsecaseBuilder.newUsecase( "Configuration:" + me.identity().get() );
            UnitOfWork buildUow = entityDescriptor.module().instance().unitOfWorkFactory().newUnitOfWork( usecase );
            Class<V> configType = serviceModel.configurationType();
            // Check for defaults
            V config = tryLoadPropertiesFile( buildUow, entityDescriptor, identity );
            if( config == null )
            {
                config = tryLoadJsonFile( buildUow, entityDescriptor, identity );
                if( config == null )
                {
                    config = tryLoadYamlFile( buildUow, entityDescriptor, identity );
                    if( config == null )
                    {
                        config = tryLoadXmlFile( buildUow, entityDescriptor, identity );
                        if( config == null )
                        {
                            config = tryLoadSystemProperties( buildUow, entityDescriptor, identity );
                            if( config == null )
                            {
                                try
                                {
                                    EntityBuilder<V> configBuilder =
                                        buildUow.newEntityBuilder( serviceModel.<V>configurationType(), identity );
                                    configBuilder.newInstance();
                                }
                                catch( ConstraintViolationException e )
                                {
                                    throw new NoSuchConfigurationException( configType, identity, e );
                                }
                            }
                        }
                    }
                }
            }

            try
            {
                buildUow.complete();

                // Try again
                return (V) findConfigurationInstanceFor( serviceModel, identity, uow );
            }
            catch( Exception e1 )
            {
                InstantiationException ex = new InstantiationException(
                    "Could not instantiate configuration, and no configuration initialization file was found ("
                    + identity + ")" );
                ex.initCause( e1 );
                throw ex;
            }
        }

        private <V> V tryLoadSystemProperties( UnitOfWork buildUow, EntityDescriptor configType, Identity identity )
            throws InstantiationException
        {
            @SuppressWarnings( "unchecked" )
            EntityBuilder<V> configBuilder = buildUow.newEntityBuilder( (Class<V>) configType.primaryType(), identity );
            PropertyMapper.map( systemProperties(), (Composite) configBuilder.instance() );
            return configBuilder.newInstance();
        }

        private Properties systemProperties()
        {
            Stream<Map.Entry<?, ?>> allProps =
                Stream.concat( System.getenv().entrySet().stream(), System.getProperties().entrySet().stream() );
            Properties props = new Properties();
            allProps.forEach( entry -> props.put( transform( (String) entry.getKey() ), entry.getValue() ) );
            return props;
        }

        private String transform( String text )
        {
            boolean upper = false;
            StringBuilder builder = new StringBuilder();
            for( int i = 0; i < text.length(); i++ )
            {
                char ch = Character.toLowerCase( text.charAt( i ) );
                if( ch == '.' )
                {
                    upper = true;
                    continue;
                }
                if( upper )
                {
                    ch = Character.toUpperCase( ch );
                    upper = false;
                }
                builder.append( ch );
            }
            return builder.toString();
        }

        private <V> V tryLoadPropertiesFile( UnitOfWork buildUow, EntityDescriptor configType, Identity identity )
            throws InstantiationException
        {
            @SuppressWarnings( "unchecked" )
            EntityBuilder<V> configBuilder = buildUow.newEntityBuilder( (Class<V>) configType.primaryType(), identity );

            String resourceName = identity + ".properties";
            try( InputStream asStream = getResource( configType.primaryType(), resourceName ) )
            {
                if( asStream != null )
                {
                    try
                    {
                        PropertyMapper.map( asStream, (Composite) configBuilder.instance() );
                        return configBuilder.newInstance();
                    }
                    catch( IOException e1 )
                    {
                        InstantiationException exception = new InstantiationException(
                            "Could not read underlying Properties file." );
                        exception.initCause( e1 );
                        throw exception;
                    }
                }
                return null;
            }
            catch( IOException ignored )
            {
                return null;
            }
        }

        private InputStream getResource( Class<?> type, String resourceName )
        {
            // Load defaults from classpath root if available
            if( type.getResource( resourceName ) == null && type.getResource( "/" + resourceName ) != null )
            {
                resourceName = "/" + resourceName;
            }
            return type.getResourceAsStream( resourceName );
        }

        private <V extends HasIdentity> V tryLoadJsonFile( UnitOfWork uow,
                                                           EntityDescriptor configType,
                                                           Identity identity
                                                         )
        {
            return readConfig( uow, configType, identity, Serialization.Format.JSON, ".json" );
        }

        private <V extends HasIdentity> V tryLoadYamlFile( UnitOfWork uow,
                                                           EntityDescriptor configType,
                                                           Identity identity
                                                         )
        {
            return readConfig( uow, configType, identity, Serialization.Format.YAML, ".yaml" );
        }

        private <V extends HasIdentity> V tryLoadXmlFile( UnitOfWork uow,
                                                          EntityDescriptor configType,
                                                          Identity identity
                                                        )
        {
            return readConfig( uow, configType, identity, Serialization.Format.XML, ".xml" );
        }

        private <V extends HasIdentity> V readConfig( UnitOfWork uow,
                                                      EntityDescriptor configType,
                                                      Identity identity,
                                                      String format,
                                                      String extension
                                                    )
        {
            Module module = configType.module().instance();
            return module.findServices( Deserializer.class )
                         .filter( ref ->
                                  {
                                      ServiceTags serviceTags = ref.metaInfo( ServiceTags.class );
                                      return serviceTags.hasTag( format );
                                  } )
                         .map( ref ->
                               {
                                   String resourceName = identity + extension;
                                   try( InputStream asStream = getResource( configType.primaryType(), resourceName ) )
                                   {
                                       if( asStream != null )
                                       {
                                           Deserializer deserializer = ref.get();
                                           V configObject = deserializer.deserialize( module.descriptor(), configType.valueType(),
                                                                                      new InputStreamReader( asStream, UTF_8 ) );

                                           @SuppressWarnings( "unchecked" )
                                           Class<V> primaryType = (Class<V>) configType.primaryType();
                                           return uow.toEntity( primaryType, configObject );
                                       }
                                   }
                                   catch( IOException ignored )
                                   {
                                   }
                                   return null;
                               } )
                         .filter( Objects::nonNull )
                         .findFirst().orElse( null );
        }
    }
}
