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
import org.qi4j.api.common.Visibility;
import org.qi4j.api.structure.Layer;

/**
 * Fluid API for declaring a layer in an application. This is obtained by calling {@link ApplicationAssembly#layer(String)}.
 */
public interface LayerAssembly
{
    /**
     * Get an assembly for a particular Module. If this is called many times with the same name, then the same module
     * is affected.
     *
     * @param name The name of the Module to retrieve or create.
     *
     * @return The ModuleAssembly for the Module.
     */
    ModuleAssembly module( String name );

    /**
     *
     * @return the {@link ApplicationAssembly} that this {@code LayerAssembly} belongs to.
     */
    ApplicationAssembly application();

    /**
     *
     * @return the name of the {@code Layer} that is being assembled.
     */
    String name();

    /**
     * Sets the name of the {@code Layer} being assembled.
     * By convention, a {@code Layer} should include the word "Layer" as the suffix, e.g "DomainLayer", so tooling
     * can use camel-case detection and present the name and not require it to explicitly say that it is a layer.
     *
     * @param name The name that the Layer should have.
     * @return this {@code LayerAssembly} instance to support fluent APIs
     */
    LayerAssembly setName( String name );

    /** Set metadata for the {@code Layer}.
     * Any arbitrary object can be attached as metainfo on a {@code Layer} that is available in runtime, by calling
     * {@link Layer#metaInfo(Class)}. Multiple registrations of the same type is
     * not possible, and the lookup will search registered meta-info objects against the lookup type, prioritizing
     * interfaces over classes and the most sub-typed nterface first.
     *
     * @param info The meta-info type to be looked up. Typically a type that is explicitly used for meta-info.
     * @return this {@code LayerAssembly} instance to support fluent APIs
     */
    LayerAssembly setMetaInfo( Object info );

    /** Declaration of which other {@code Layers} that this {@code Layer} is able to use.
     * This is how architecture reinforcement works. Any composites that are declared
     * {@link Visibility#application} in a "used" layer can be reached from this Layer.
     *
     * @param layerAssembly The {@code Layer(s)} that this {@code Layer} can reach/see.
     * @return this {@code LayerAssembly} instance to support fluent APIs
     */
    LayerAssembly uses( LayerAssembly... layerAssembly );

    /**
     * Set the layer activators. Activators are executed in order around the
     * Layer activation and passivation.
     *
     * @param activators the layer activators
     * @return the assembly
     */    
    @SuppressWarnings( { "unchecked","varargs" } )
    LayerAssembly withActivators( Class<? extends Activator<Layer>>... activators );

    /**
     * The visitor pattern to inspect the entire pre-instantiated model.
     *
     * @deprecated New mechanism is considered, using Java 8 Stream API.
     *
     * @param visitor The visitor to be called.
     * @param <ThrowableType> The exceptions that may be thrown.
     * @throws ThrowableType when there is an underlying problem in the model.
     */
    @Deprecated
    <ThrowableType extends Throwable> void visit( AssemblyVisitor<ThrowableType> visitor )
        throws ThrowableType;

    /**
     * Given a Specification for EntityAssembly's, returns a EntityDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification The Specification that specifies the EntityComposite types of interest.
     *
     * @return An EntityDeclaration for the specified EntityComposite types.
     */
    EntityDeclaration entities( Predicate<? super EntityAssembly> specification );

    /**
     * Given a Specification for ServiceAssembly's, returns a ServiceDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification The Specification that specifies the ServiceComposite types of interest.
     *
     * @return An ServiceDeclaration for the specified ServiceComposite types.
     */
    ServiceDeclaration services( Predicate<? super ServiceAssembly> specification );

    /**
     * Given a Specification for TransientAssembly's, returns a TransientDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification The Specification that specifies the TransientComposite types of interest.
     *
     * @return An TransientDeclaration for the specified TransientComposite types.
     */
    TransientDeclaration transients( Predicate<? super TransientAssembly> specification );

    /**
     * Given a Specification for ValueAssembly's, returns a ValueDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification The Specification that specifies the ValueComposite types of interest.
     *
     * @return An ValueDeclaration for the specified ValueComposite types.
     */
    ValueDeclaration values( Predicate<? super ValueAssembly> specification );

    /**
     * Given a Specification for ObjectAssembly's, returns a ObjectDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification The Specification that specifies the Object types of interest.
     *
     * @return An ObjectDeclaration for the specified Object types.
     */
    ObjectDeclaration objects( Predicate<? super ObjectAssembly> specification );

    /**
     * Given a Specification for ImportedServiceAssembly's, returns a ImportedServiceDeclaration that can
     * be used to work with all of the assemblies in this Layer matched by the specification.
     *
     * @param specification The Specification that specifies the Imported Service types of interest.
     *
     * @return An ImportedServiceDeclaration for the specified Imported Service types.
     */
    ImportedServiceDeclaration importedServices( Predicate<? super ImportedServiceAssembly> specification );
}
