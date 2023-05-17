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

package org.qi4j.bootstrap;

import java.util.function.Predicate;
import org.qi4j.api.activation.Activator;
import org.qi4j.api.identity.IdentityGenerator;
import org.qi4j.api.metrics.MetricsProvider;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.HasTypes;
import org.qi4j.api.serialization.Serialization;

/**
 * The ModuleAssembly is used to register any information about * what the module should contain, such as composites,
 * entities and services.
 * <p>
 * Use the methods and the fluent API's to declare how the module should be constructed.
 * </p>
 */
public interface ModuleAssembly
{
    /**
     * Access the layer assembly for this module.
     *
     * @return The Layer containing this Module.
     */
    LayerAssembly layer();

    /**
     * Get an assembly for a particular Module. If this is called many times with the same names, then the same module
     * is affected.
     *
     * @param layerName  The name of the Layer
     * @param moduleName The name of the Module to retrieve or create.
     *
     * @return The ModuleAssembly for the Module.
     */
    ModuleAssembly module( String layerName, String moduleName );

    /**
     * Set the name of this module.
     *
     * @param name The name that this Module should have.
     *
     * @return This instance to support the fluid DSL of bootstrap.
     */
    ModuleAssembly setName( String name );

    /**
     * Access the currently set name for this module.
     *
     * @return The name of this Module.
     */
    String name();

    ModuleAssembly setMetaInfo( Object info );

    /**
     * Assemble default services for {@link IdentityGenerator},
     * {@link Serialization} and
     * {@link MetricsProvider}.
     *
     * If one of this default services is already assembled on this module it is not added.
     * The returned {@link ServiceDeclaration} only apply to effectively defaulted services.
     *
     * @return the declaration for assembled services
     */
    ServiceDeclaration defaultServices();

    /**
     * Set the module activators. Activators are executed in order around the
     * Module activation and passivation.
     *
     * @param activators the module activators
     *
     * @return the assembly
     */
    @SuppressWarnings( { "unchecked", "varargs" } )
    ModuleAssembly withActivators( Class<? extends Activator<Module>>... activators );

    /**
     * Declare a list of TransientComposites for this Module. Use the TransientDeclaration that is returned to
     * declare further settings. Note that the TransientDeclaration works on all of the types specified.
     *
     * @param transientTypes The types that specifies the Transient types.
     *
     * @return A TransientDeclaration for the specified Transient types.
     */
    TransientDeclaration transients( Class<?>... transientTypes );

    /**
     * Given a Specification for TransientAssembly's, returns a TransientDeclaration that can
     * be used to work with all of the assemblies matched by the specification.
     *
     * @param specification The Specification that specifies the TransientComposite types of interest.
     *
     * @return A TransientDeclaration for the specified TransientComposite types.
     */
    TransientDeclaration transients( Predicate<? super TransientAssembly> specification );

    /**
     * Declare a list of ValueComposites for this Module. Use the ValueDeclaration that is returned to
     * declare further settings. Note that the ValueDeclaration works on all of the types specified.
     *
     * @param valueTypes The types that specifies the Value types.
     *
     * @return A ValueDeclaration for the specified Value types.
     */
    ValueDeclaration values( Class<?>... valueTypes );

    /**
     * Given a Specification for ValueAssembly's, returns a ValueDeclaration that can
     * be used to work with all of the assemblies matched by the specification.
     *
     * @param specification The Specification that specifies the ValueComposite types of interest.
     *
     * @return A ValueDeclaration for the specified ValueComposite types.
     */
    ValueDeclaration values( Predicate<? super ValueAssembly> specification );

    /**
     * Declare a list of EntityComposites for this Module. Use the EntityDeclaration that is returned to
     * declare further settings. Note that the EntityDeclaration works on all of the types specified.
     *
     * @param entityTypes The types that specifies the Entity types.
     *
     * @return An EntityDeclaration for the specified Entity types.
     */
    EntityDeclaration entities( Class<?>... entityTypes );

    /**
     * Given a Specification for EntityAssembly's, returns a EntityDeclaration that can
     * be used to work with all of the assemblies matched by the specification.
     *
     * @param specification The Specification that specifies the EntityComposite types of interest.
     *
     * @return An EntityDeclaration for the specified EntityComposite types.
     */
    EntityDeclaration entities( Predicate<? super EntityAssembly> specification );

    /**
     * Declare a list of Configuration Composites for this Module. Use the ConfigurationDeclaration that is returned to
     * declare further settings. Note that the ConfigurationDeclaration works on all of the types specified.
     *
     * @param configurationTypes The types that specifies the Configuration types.
     *
     * @return A ConfigurationDeclaration for the specified Configuration types.
     */
    ConfigurationDeclaration configurations( Class<?>... configurationTypes );

    /**
     * Given a Specification for Configuration, returns a ConfigurationDeclaration that can be used to work with all
     * of the assemblies matched by the specification.
     *
     * @param specification The Specification that specifies the ConfigurationComposite types of interest.
     *
     * @return A ConfigurationDeclaration for the specified EntityComposite types.
     */
    ConfigurationDeclaration configurations( Predicate<HasTypes> specification );

    /**
     * Declare a list of object classes for this Module. Use the ObjectDeclaration that is returned to
     * declare further settings. Note that the ObjectDeclaration works on all of the types specified.
     *
     * @param objectTypes The types that specifies the Object types.
     *
     * @return An ObjectDeclaration for the specified Object types.
     *
     * @throws AssemblyException on invalid assembly
     */
    ObjectDeclaration objects( Class<?>... objectTypes )
        throws AssemblyException;

    /**
     * Given a Specification for ObjectAssembly's, returns a ObjectDeclaration that can
     * be used to work with all of the assemblies matched by the specification.
     *
     * @param specification The Specification that specifies the Object types of interest.
     *
     * @return An ObjectDeclaration for the specified Object types.
     */
    ObjectDeclaration objects( Predicate<? super ObjectAssembly> specification );

    /**
     * Create a list of ServiceComposites for this Module. Use the ServiceDeclaration that is returned to
     * declare further settings. This will always create new assemblies for the specified types, instead
     * of potentially working on already declared types like the services(...) method.
     *
     * @param serviceTypes The types that specifies the Service types.
     *
     * @return A ServiceDeclaration for the specified Service types.
     */
    ServiceDeclaration addServices( Class<?>... serviceTypes );

    /**
     * Declare a list of ServiceComposites for this Module. Use the ServiceDeclaration that is returned to
     * declare further settings. Note that the ServiceDeclaration works on all of the types specified.
     *
     * @param serviceTypes The types that specifies the Service types.
     *
     * @return A ServiceDeclaration for the specified Service types.
     */
    ServiceDeclaration services( Class<?>... serviceTypes );

    /**
     * Given a Specification for ServiceAssembly's, returns a ServiceDeclaration that can
     * be used to work with all of the assemblies matched by the specification.
     *
     * @param specification The Specification that specifies the ServiceComposite types of interest.
     *
     * @return A ServiceDeclaration for the specified ServiceComposite types.
     */
    ServiceDeclaration services( Predicate<? super ServiceAssembly> specification );

    /**
     * Declare a list of imported services for this Module. Use the ImportedServiceDeclaration that is returned to
     * declare further settings. Note that the ImportedServiceDeclaration works on all of the types specified.
     *
     * @param serviceTypes The types that specifies the Imported Service types.
     *
     * @return An ImportedServiceDeclaration for the specified Imported Service types.
     */
    ImportedServiceDeclaration importedServices( Class<?>... serviceTypes );

    /**
     * Given a Specification for ImportedServiceAssembly's, returns a ImportedServiceDeclaration that can
     * be used to work with all of the assemblies matched by the specification.
     *
     * @param specification The Specification that specifies the Imported Service types of interest.
     *
     * @return An ImportedServiceDeclaration for the specified Imported Service types.
     */
    ImportedServiceDeclaration importedServices( Predicate<? super ImportedServiceAssembly> specification );

    <T> MixinDeclaration<T> forMixin( Class<T> mixinType );

    <ThrowableType extends Throwable> void visit( AssemblyVisitor<ThrowableType> visitor )
        throws ThrowableType;
}
