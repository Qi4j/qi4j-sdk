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

package org.qi4j.runtime.injection;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Predicate;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.Optional;
import org.qi4j.api.identity.Identity;
import org.qi4j.api.identity.StringIdentity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.qualifier.AnnotationQualifier;
import org.qi4j.api.service.qualifier.IdentifiedBy;
import org.qi4j.api.service.qualifier.Qualifier;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.bootstrap.SingletonAssembler;
import org.junit.jupiter.api.Test;
import org.qi4j.bootstrap.*;

import static org.qi4j.api.common.Visibility.application;
import static org.qi4j.api.common.Visibility.layer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test the @Service injection annotation
 */
public class ServiceInjectionTest
{
    @Test
    public void testInjectService()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler(
            module -> {
                module.services( MyServiceComposite.class )
                      .identifiedBy( "Foo" )
                      .setMetaInfo( new ServiceName( "Foo" ) );
                module.services( MyServiceComposite2.class )
                      .identifiedBy( "Bar" )
                      .setMetaInfo( new ServiceName( "Bar" ) );
                module.services( StringService.class, LongService.class );
                module.objects( ServiceUser.class );
            }
        );

        testInjection( assembly );
    }

    private void testInjection( SingletonAssembler assembly )
    {
        ObjectFactory factory = assembly.module();
        ServiceUser user = factory.newObject( ServiceUser.class );

        assertThat( user.testSingle(), equalTo( "X" ) );
        assertThat( user.testIdentity(), equalTo( StringIdentity.identityOf( "Foo" ) ) );
        assertThat( user.testServiceReference(), equalTo( "FooX" ) );
        assertThat( user.testQualifier(), equalTo( "Bar" ) );
        assertThat( user.testStringIterable(), equalTo( "A" ) );
        assertThat( user.testLongIterable(), equalTo( 1L ) );
        assertThat( user.testIterableServiceReferences(), equalTo( "FooXBarX" ) );
        assertThat( user.testIterable(), equalTo( "XX" ) );
    }

    @Test
    public void testInjectionServiceBetweenModules()
        throws ActivationException
    {
        SingletonAssembler assembly = new SingletonAssembler(
            module -> {
                module.services( MyServiceComposite.class )
                      .identifiedBy( "Foo" )
                      .setMetaInfo( new ServiceName( "Foo" ) );
                module.services( StringService.class, LongService.class );
                module.objects( ServiceUser.class );

                ModuleAssembly module2 = module.layer().module( "Other module" );
                ServiceDeclaration service2Decl = module2.services( MyServiceComposite.class );
                service2Decl.identifiedBy( "Bar" ).setMetaInfo( new ServiceName( "Bar" ) ).visibleIn( layer );

                ServiceDeclaration service3Decl = module2.services( MyServiceComposite2.class );
                service3Decl.identifiedBy( "Boo" ).setMetaInfo( new ServiceName( "Boo" ) );
            }
        );

        testInjection( assembly );
    }

    @Test
    public void testInjectionServiceBetweenLayers()
        throws ActivationException
    {
        SingletonAssembler assembly = new SingletonAssembler(
            module -> {
                module.services( MyServiceComposite.class )
                      .identifiedBy( "Foo" )
                      .setMetaInfo( new ServiceName( "Foo" ) );
                module.services( StringService.class, LongService.class );
                LayerAssembly layerAssembly = module.layer();
                module.objects( ServiceUser.class );

                ApplicationAssembly applicationAssembly = layerAssembly.application();
                LayerAssembly layer2Assembly = applicationAssembly.layer( "Other layer" );
                layerAssembly.uses( layer2Assembly );

                ModuleAssembly module2 = layer2Assembly.module( "Other module" );

                ServiceDeclaration service2Decl = module2.services( MyServiceComposite2.class );
                service2Decl.identifiedBy( "Bar" ).setMetaInfo( new ServiceName( "Bar" ) ).visibleIn( application );
            }
        );

        testInjection( assembly );
    }

    @Test
    public void testMissingServiceDependency()
        throws ActivationException
    {
        assertThrows( ConstructionException.class, () -> {
            // No service fulfils the dependency injection -> fail to create application
            new SingletonAssembler( module -> module.objects( ServiceUser.class ) )
                .module().newObject( ServiceUser.class );
        } );
    }

    @Mixins( MyServiceMixin.class )
    public interface MyServiceComposite
        extends MyService, ServiceComposite
    {
    }

    public interface MyServiceComposite2
        extends MyServiceComposite
    {
    }

    public interface MyService
    {
        String doStuff();
    }

    public static class MyServiceMixin
        implements MyService
    {

        public String doStuff()
        {
            return "X";
        }
    }

    public static class ServiceUser
        extends AbstractServiceUser<String>
    {
    }

    public static class AbstractServiceUser<T>
    {
        @Service
        MyService service;

        @Service
        MyServiceComposite serviceComposite;

        @Service
        Iterable<MyService> services;

        @Service
        ServiceReference<MyService> serviceRef;

        @Service
        Iterable<ServiceReference<MyService>> serviceRefs;

        @Service
        @IdentifiedBy( "Bar" )
        ServiceReference<MyService> qualifiedService;

        @Service
        @IdentifiedBy( "Bar" )
        Iterable<ServiceReference<MyService>> qualifiedServiceRefs;

        @Optional
        @Service
        MyServiceMixin optionalService12;

        @Service
        Foo<Long> longService;

        @Service
        Foo<T> stringService;

        public String testSingle()
        {
            return service.doStuff();
        }

        public Identity testIdentity()
        {
            return serviceComposite.identity().get();
        }

        public String testIterable()
        {
            String str = "";
            for( MyService myService : services )
            {
                str += myService.doStuff();
            }
            return str;
        }

        public String testServiceReference()
            throws ServiceImporterException
        {
            ServiceName info = serviceRef.metaInfo( ServiceName.class );
            return info.getName() + serviceRef.get().doStuff();
        }

        public String testIterableServiceReferences()
            throws ServiceImporterException
        {
            String str = "";
            for( ServiceReference<MyService> serviceReference : serviceRefs )
            {
                str += serviceReference.metaInfo( ServiceName.class ).getName();
                str += serviceReference.get().doStuff();
            }
            return str;
        }

        public String testQualifier()
        {
            return qualifiedService.metaInfo( ServiceName.class ).getName();
        }

        public String testQualifiedServices()
        {
            String str = "";
            for( ServiceReference<MyService> qualifiedServiceRef : qualifiedServiceRefs )
            {
                str += qualifiedServiceRef.metaInfo( ServiceName.class ).getName();
            }
            return str;
        }

        public T testStringIterable()
        {
            return stringService.get();
        }

        public Long testLongIterable()
        {
            return longService.get();
        }
    }

    @Qualifier( NamedSelector.class )
    @Retention( RetentionPolicy.RUNTIME )
    public @interface Named
    {
        String value();
    }

    public static final class NamedSelector
        implements AnnotationQualifier<Named>
    {
        public <T> Predicate<ServiceReference<?>> qualifier( final Named named )
        {
            return new Predicate<ServiceReference<?>>()
            {
                public boolean test( ServiceReference<?> service )
                {
                    ServiceName serviceName = service.metaInfo( ServiceName.class );
                    return ( serviceName != null && serviceName.getName().equals( named.value() ) );
                }
            };
        }
    }

    public static class ServiceName
        implements Serializable
    {
        private String name;

        public ServiceName( String name )
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }

    public interface Foo<T>
    {
        T get();
    }

    @Mixins( StringService.Mixin.class )
    public interface StringService
        extends Foo<String>, ServiceComposite
    {
        class Mixin
            implements Foo<String>
        {
            @Override
            public String get()
            {
                return "A";
            }
        }
    }

    @Mixins( LongService.Mixin.class )
    public interface LongService
        extends Foo<Long>, ServiceComposite
    {
        class Mixin
            implements Foo<Long>
        {
            @Override
            public Long get()
            {
                return 1L;
            }
        }
    }
}
