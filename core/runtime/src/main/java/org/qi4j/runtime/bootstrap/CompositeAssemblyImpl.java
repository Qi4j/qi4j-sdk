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
package org.qi4j.runtime.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.GenericAssociationInfo;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.injection.scope.State;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Property;
import org.qi4j.api.sideeffect.SideEffects;
import org.qi4j.api.type.HasTypes;
import org.qi4j.api.util.Annotations;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.Fields;
import org.qi4j.api.util.HierarchicalVisitorAdapter;
import org.qi4j.bootstrap.AssemblyReportException;
import org.qi4j.bootstrap.StateDeclarations;
import org.qi4j.runtime.association.AssociationModel;
import org.qi4j.runtime.association.AssociationsModel;
import org.qi4j.runtime.association.ManyAssociationModel;
import org.qi4j.runtime.association.ManyAssociationsModel;
import org.qi4j.runtime.association.NamedAssociationModel;
import org.qi4j.runtime.association.NamedAssociationsModel;
import org.qi4j.runtime.composite.AbstractConstraintModel;
import org.qi4j.runtime.composite.CompositeConstraintModel;
import org.qi4j.runtime.composite.CompositeMethodModel;
import org.qi4j.runtime.composite.CompositeMethodsModel;
import org.qi4j.runtime.composite.ConcernModel;
import org.qi4j.runtime.composite.ConcernsModel;
import org.qi4j.runtime.composite.ConstraintModel;
import org.qi4j.runtime.composite.ConstraintsModel;
import org.qi4j.runtime.composite.GenericPredicate;
import org.qi4j.runtime.composite.InterfaceDefaultMethodsMixin;
import org.qi4j.runtime.composite.MixinModel;
import org.qi4j.runtime.composite.MixinsModel;
import org.qi4j.runtime.composite.SideEffectModel;
import org.qi4j.runtime.composite.SideEffectsModel;
import org.qi4j.runtime.composite.StateModel;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.composite.ValueConstraintsModel;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.property.PropertiesModel;
import org.qi4j.runtime.property.PropertyModel;
import org.qi4j.bootstrap.AssemblyReportException;
import org.qi4j.bootstrap.StateDeclarations;
import org.qi4j.runtime.association.*;
import org.qi4j.runtime.composite.*;
import org.qi4j.runtime.injection.Dependencies;
import org.qi4j.runtime.injection.DependencyModel;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.qi4j.api.composite.InvalidCompositeException.handleInvalidCompositeType;
import static org.qi4j.api.util.Annotations.isType;
import static org.qi4j.api.util.Annotations.typeHasAnnotation;
import static org.qi4j.api.util.Classes.classHierarchy;
import static org.qi4j.api.util.Classes.interfacesOf;
import static org.qi4j.api.util.Classes.isAssignableFrom;
import static org.qi4j.api.util.Classes.typeOf;
import static org.qi4j.api.util.Classes.typesOf;
import static org.qi4j.api.util.Classes.wrapperClass;

/**
 * Declaration of a Composite.
 */
public abstract class CompositeAssemblyImpl
    implements HasTypes
{
    List<Class<?>> concerns = new ArrayList<>();
    List<Class<?>> sideEffects = new ArrayList<>();
    List<Class<?>> mixins = new ArrayList<>();
    List<Class<?>> types = new ArrayList<>();
    MetaInfo metaInfo = new MetaInfo();
    Visibility visibility = Visibility.module;

    private boolean immutable;
    PropertiesModel propertiesModel;
    StateModel stateModel;
    MixinsModel mixinsModel;
    CompositeMethodsModel compositeMethodsModel;
    private AssemblyHelper helper;
    private StateDeclarations stateDeclarations;

    private Set<String> registeredStateNames = new HashSet<>();

    CompositeAssemblyImpl( Class<?> mainType )
    {
        types.add( mainType );
    }

    @Override
    public Stream<Class<?>> types()
    {
        return types.stream();
    }

    protected StateModel createStateModel()
    {
        return new StateModel( propertiesModel );
    }

    protected MixinsModel createMixinsModel()
    {
        return new MixinsModel();
    }

    void buildComposite( AssemblyHelper helper,
                         StateDeclarations stateDeclarations
                       )
    {
        this.stateDeclarations = stateDeclarations;
        this.helper = helper;
        for( Class<?> compositeType : types )
        {
            metaInfo = new MetaInfo( metaInfo ).withAnnotations( compositeType );
            addAnnotationsMetaInfo( compositeType, metaInfo );
        }

        immutable = metaInfo.get( Immutable.class ) != null;
        propertiesModel = new PropertiesModel();
        stateModel = createStateModel();
        mixinsModel = createMixinsModel();
        compositeMethodsModel = new CompositeMethodsModel( mixinsModel );

        // Implement composite methods
        List<Class<?>> constraintClasses = toList( constraintDeclarations( getAllTypes() ) );
        List<Class<?>> concernClasses = toList( concat( concerns.stream(), concernDeclarations( getAllTypes() ) ) );
        List<Class<?>> sideEffectClasses = toList( concat( sideEffects.stream(), sideEffectDeclarations( getAllTypes() ) ) );
        List<Class<?>> mixinClasses = toList( concat( mixins.stream(), mixinDeclarations( getAllTypes() ) ) );
        //noinspection unchecked
        implementMixinType( types,
                            constraintClasses,
                            concernClasses,
                            sideEffectClasses,
                            mixinClasses
                          );

        // Add state from methods and fields
        //noinspection unchecked
        addState( constraintClasses );
    }

    private List<Class<?>> toList( Stream<Class<?>> stream )
    {
        return stream.collect( Collectors.toList() );
    }

    private void addAnnotationsMetaInfo( Class<?> type, MetaInfo compositeMetaInfo )
    {
        Class[] declaredInterfaces = type.getInterfaces();
        for( int i = declaredInterfaces.length - 1; i >= 0; i-- )
        {
            addAnnotationsMetaInfo( declaredInterfaces[ i ], compositeMetaInfo );
        }
        compositeMetaInfo.withAnnotations( type );
    }

    private void implementMixinType( List<? extends Class<?>> types,
                                     List<Class<?>> constraintClasses,
                                     List<Class<?>> concernClasses,
                                     List<Class<?>> sideEffectClasses,
                                     List<Class<?>> mixinClasses
                                   )
    {
        Set<Throwable> exceptions = new HashSet<>();
        Set<Class<?>> thisDependencies = new HashSet<>();
        types.stream()
             .peek( mixinType -> mixinsModel.addMixinType( mixinType ) )
             .flatMap( mixinType -> Arrays.stream( mixinType.getMethods() ) )
             .forEach( method -> implementMixinMethod( method, mixinClasses, constraintClasses,
                                                       concernClasses,
                                                       sideEffectClasses,
                                                       exceptions,
                                                       thisDependencies
                                                     )
                     );

        // Implement all @This dependencies that were found
        thisDependencies.forEach(
            thisDependency ->
            {
                // Add additional declarations from the @This type
                Stream<Class<?>> typeConstraintClasses = concat(
                    constraintClasses.stream(),
                    constraintDeclarations( thisDependency ) );
                Stream<Class<?>> typeConcernClasses = concat(
                    concernClasses.stream(),
                    concernDeclarations( thisDependency ) );
                Stream<Class<?>> typeSideEffectClasses = concat(
                    sideEffectClasses.stream(),
                    sideEffectDeclarations( thisDependency ) );
                Stream<Class<?>> typeMixinClasses = concat(
                    mixinClasses.stream(),
                    mixinDeclarations( thisDependency ) );
                List<? extends Class<?>> singleton = Collections.singletonList( thisDependency );
                implementMixinType( singleton,
                                    toList( typeConstraintClasses ),
                                    toList( typeConcernClasses ),
                                    toList( typeSideEffectClasses ),
                                    toList( typeMixinClasses )
                                  );
            } );
        if( exceptions.size() > 0 )
        {
            throw new AssemblyReportException( exceptions );
        }
    }

    private void implementMixinMethod( Method method,
                                       List<Class<?>> mixinClasses,
                                       List<Class<?>> constraintClasses,
                                       List<Class<?>> concernClasses,
                                       List<Class<?>> sideEffectClasses,
                                       Set<Throwable> exceptions,
                                       Set<Class<?>> thisDependencies )
    {
        try
        {
            if( !compositeMethodsModel.isImplemented( method )
                && !Proxy.class.equals( method.getDeclaringClass().getSuperclass() )
                && !Proxy.class.equals( method.getDeclaringClass() )
                && !Modifier.isStatic( method.getModifiers() ) )
            {
                MixinModel mixinModel = implementMethod( method, mixinClasses );
                if( mixinModel != null )
                {
                    ConcernsModel concernsModel = concernsFor(
                        method,
                        mixinModel.mixinClass(),
                        concat( concernDeclarations( mixinModel.mixinClass() ),
                                concernClasses.stream() )
                                                             );
                    SideEffectsModel sideEffectsModel = sideEffectsFor(
                        method,
                        mixinModel.mixinClass(),
                        concat( sideEffectDeclarations( mixinModel.mixinClass() ),
                                sideEffectClasses.stream() )
                                                                      );
                    Stream<Class<?>> concat = concat( constraintDeclarations( mixinModel.mixinClass() ),
                                                      constraintClasses.stream() );
                    ConstraintsModel constraints = constraintsFor( method, toList( concat ) );
                    CompositeMethodModel methodComposite = new CompositeMethodModel(
                        method,
                        constraints,
                        concernsModel,
                        sideEffectsModel,
                        mixinsModel
                    );

                    Stream<? extends Dependencies> source = of( methodComposite, mixinModel );
                    source.flatMap( Dependencies::dependencies )
                          .filter( new DependencyModel.ScopeSpecification( This.class ) )
                          .map( DependencyModel::rawInjectionType )
                          .forEach( thisDependencies::add );

                    interfacesOf( mixinModel.mixinClass() )
                        .map( Classes.RAW_CLASS )
                        .filter( clazz -> of( Initializable.class, Lifecycle.class, InvocationHandler.class )
                            .noneMatch( c -> c.equals( clazz ) ) )
                        .forEach( thisDependencies::add );

                    compositeMethodsModel.addMethod( methodComposite );
                }
            }
        }
        catch( Exception e )
        {
            exceptions.add( e );
        }
    }

    @SuppressWarnings( "raw" )
    private MixinModel implementMethod( Method method, List<Class<?>> mixinDeclarations )
    {
        MixinModel implementationModel = mixinsModel.mixinFor( method );
        if( implementationModel != null )
        {
            return implementationModel;
        }
        Class mixinClass = findTypedImplementation( method, mixinDeclarations.stream() );
        if( mixinClass != null )
        {
            return implementMethodWithClass( method, mixinClass );
        }

        // Check generic implementations
        mixinClass = findGenericImplementation( method, concat( mixinDeclarations.stream(), of( InterfaceDefaultMethodsMixin.class ) ) );
        if( mixinClass != null )
        {
            return implementMethodWithClass( method, mixinClass );
        }
        handleInvalidCompositeType( "No implementation found for method", null, null, null, null, method, types );
        return null;
    }

    private Class<?> findTypedImplementation( final Method method, Stream<Class<?>> mixins )
    {
        // Check if mixinClass implements the method. If so, check if the mixinClass is generic or if the filter passes.
        // If a mixinClass is both generic AND non-generic at the same time, then the filter applies to the non-generic
        // side only.
        Predicate<Class<?>> appliesToSpec = item -> helper.appliesTo( item, method, types, item );
        return mixins.filter( isAssignableFrom( method.getDeclaringClass() )
                                  .and( GenericPredicate.INSTANCE.or( appliesToSpec ) ) )
                     .findFirst().orElse( null );
    }

    private Class<?> findGenericImplementation( final Method method, Stream<Class<?>> mixins )
    {
        // Check if mixinClass is generic and the applies-to filter passes
        Predicate<Class<?>> appliesToSpec = item -> helper.appliesTo( item, method, types, item );
        return mixins.filter( GenericPredicate.INSTANCE.and( appliesToSpec ) ).findFirst().orElse( null );
    }

    private MixinModel implementMethodWithClass( Method method, Class mixinClass )
    {
        MixinModel mixinModel = mixinsModel.getMixinModel( mixinClass );
        if( mixinModel == null )
        {
            mixinModel = helper.getMixinModel( mixinClass );
            mixinsModel.addMixinModel( mixinModel );
        }
        mixinsModel.addMethodMixin( method, mixinModel );
        return mixinModel;
    }

    private void addState( final List<Class<?>> constraintClasses )
    {
        // Add method state
        compositeMethodsModel.accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited )
                throws RuntimeException
            {
                if( visited instanceof CompositeMethodModel )
                {
                    CompositeMethodModel methodModel = (CompositeMethodModel) visited;
                    if( methodModel.method().getParameterTypes().length == 0 )
                    {
                        addStateFor( methodModel.method(), constraintClasses );
                    }

                    return false;
                }

                return super.visitEnter( visited );
            }
        } );

        // Add field state
        mixinsModel.accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited )
                throws RuntimeException
            {
                if( visited instanceof MixinModel )
                {
                    MixinModel model = (MixinModel) visited;
                    Consumer<Field> addState = field -> addStateFor( field, constraintClasses );
                    Fields.FIELDS_OF.apply( model.mixinClass() )
                                    .filter( Annotations.hasAnnotation( State.class ) )
                                    .forEach( addState );
                    return false;
                }
                return super.visitEnter( visited );
            }
        } );
    }

    private void addStateFor( AccessibleObject accessor, List<Class<?>> constraintClasses )
    {
        String stateName = QualifiedName.fromAccessor( accessor ).name();

        if( registeredStateNames.contains( stateName ) )
        {
            return; // Skip already registered names
        }

        Class<?> accessorType = Classes.RAW_CLASS.apply( typeOf( accessor ) );
        if( Property.class.isAssignableFrom( accessorType ) )
        {
            propertiesModel.addProperty( newPropertyModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
        else if( Association.class.isAssignableFrom( accessorType ) )
        {
            associationsModel().addAssociation( newAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
        else if( ManyAssociation.class.isAssignableFrom( accessorType ) )
        {
            manyAssociationsModel().addManyAssociation( newManyAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
        else if( NamedAssociation.class.isAssignableFrom( accessorType ) )
        {
            namedAssociationsModel().addNamedAssociation( newNamedAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
    }

    protected AssociationsModel associationsModel()
    {
        return null;
    }

    protected ManyAssociationsModel manyAssociationsModel()
    {
        return null;
    }

    protected NamedAssociationsModel namedAssociationsModel()
    {
        return null;
    }

    private PropertyModel newPropertyModel( AccessibleObject accessor,
                                            List<Class<?>> constraintClasses
                                          )
    {
        List<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn( accessor );
        boolean optional = annotations.stream().anyMatch( isType( Optional.class ) );
        ValueConstraintsModel valueConstraintsModel = constraintsFor(
            annotations.stream(),
            GenericPropertyInfo.propertyTypeOf( accessor ),
            ( (Member) accessor ).getName(),
            optional,
            constraintClasses,
            accessor );
        ValueConstraintsInstance valueConstraintsInstance = valueConstraintsModel.newInstance();
        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        UseDefaults useDefaultsDeclaration = metaInfo.get( UseDefaults.class );
        Object initialValue = stateDeclarations.initialValueOf( accessor );
        if( initialValue == null && useDefaultsDeclaration != null )
        {
            initialValue = useDefaultsDeclaration.value();
        }
        boolean useDefaults = useDefaultsDeclaration != null || stateDeclarations.useDefaults( accessor );
        boolean immutable = this.immutable || metaInfo.get( Immutable.class ) != null;
        return new PropertyModel(
            accessor,
            immutable,
            useDefaults,
            valueConstraintsInstance,
            metaInfo,
            initialValue
        );
    }

    // Model
    private ConstraintsModel constraintsFor( Method method,
                                             List<Class<?>> constraintClasses
                                           )
    {
        List<ValueConstraintsModel> parameterConstraintModels = Collections.emptyList();

        Parameter[] parameters = method.getParameters();
        Type[] parameterTypes = method.getGenericParameterTypes();
        boolean constrained = false;
        for( int i = 0; i < parameters.length; i++ )
        {
            Parameter param = parameters[i];

            Annotation[] parameterAnnotation = param.getAnnotations();

            Name nameAnnotation = (Name) of( parameterAnnotation ).filter( isType( Name.class ) )
                                                                  .findFirst().orElse( null );
            String name = nameAnnotation == null ? param.getName() : nameAnnotation.value();
            boolean optional = of( parameterAnnotation )
                .anyMatch( isType( Optional.class ) );
            ValueConstraintsModel parameterConstraintsModel = constraintsFor(
                Arrays.stream( parameterAnnotation ),
                parameterTypes[ i ],
                name,
                optional,
                constraintClasses,
                method );
            if( parameterConstraintsModel.isConstrained() )
            {
                constrained = true;
            }

            if( parameterConstraintModels.isEmpty() )
            {
                parameterConstraintModels = new ArrayList<>();
            }
            parameterConstraintModels.add( parameterConstraintsModel );
        }

        if( !constrained )
        {
            return new ConstraintsModel( Collections.emptyList() );
        }
        else
        {
            return new ConstraintsModel( parameterConstraintModels );
        }
    }

    private ValueConstraintsModel constraintsFor(
        Stream<Annotation> constraintAnnotations,
        Type valueType,
        String name,
        boolean optional,
        Iterable<Class<?>> constraintClasses,
        AccessibleObject accessor
                                                )
    {
        valueType = wrapperClass( valueType );

        List<AbstractConstraintModel> constraintModels = new ArrayList<>();
        List<Annotation> filtered = constraintAnnotations
            .filter( typeHasAnnotation( ConstraintDeclaration.class ) )
            .collect( Collectors.toList() );

        // TODO: This massive block below should be cleaned up.
        nextConstraint:
        for( Annotation constraintAnnotation : filtered )
        {
            // Check composite declarations first
            Class<? extends Annotation> annotationType = constraintAnnotation.annotationType();
            for( Class<?> constraint : constraintClasses )
            {
                @SuppressWarnings( "unchecked" )
                Class<? extends Constraint<?, ?>> constraintType = (Class<? extends Constraint<?, ?>>) constraint;
                if( helper.appliesTo( constraintType, annotationType, valueType ) )
                {
                    constraintModels.add( new ConstraintModel( constraintAnnotation, constraintType ) );
                    continue nextConstraint;
                }
            }

            // Check the annotation itself
            Constraints constraints = annotationType.getAnnotation( Constraints.class );
            if( constraints != null )
            {
                for( Class<? extends Constraint<?, ?>> constraintClass : constraints.value() )
                {
                    if( helper.appliesTo( constraintClass, annotationType, valueType ) )
                    {
                        constraintModels.add( new ConstraintModel( constraintAnnotation, constraintClass ) );
                        continue nextConstraint;
                    }
                }
            }

            // No implementation found!
            // Check if if it's a composite constraints
            if( typeHasAnnotation( ConstraintDeclaration.class ).test( constraintAnnotation ) )
            {
                ValueConstraintsModel valueConstraintsModel = constraintsFor(
                    Arrays.stream( annotationType.getAnnotations() ),
                    valueType,
                    name,
                    optional,
                    constraintClasses,
                    accessor );
                CompositeConstraintModel compositeConstraintModel = new CompositeConstraintModel(
                    constraintAnnotation,
                    valueConstraintsModel );
                constraintModels.add( compositeConstraintModel );
            }
            else
            {
                handleInvalidCompositeType( "Cannot find implementation of constraint @", null, annotationType, null, valueType, (Member) accessor, types );
            }
        }
        return new ValueConstraintsModel( constraintModels, name, optional );
    }

    private ConcernsModel concernsFor( Method method,
                                       Class<?> mixinClass,
                                       Stream<Class<?>> concernClasses
                                     )
    {
        List<ConcernModel> concernsFor = new ArrayList<>();
        concernClasses.forEach( concern ->
                                {
                                    if( helper.appliesTo( concern, method, types, mixinClass ) )
                                    {
                                        addConcernOrRepositionIfExists( concernsFor, helper.getConcernModel( concern ) );
                                    }
                                    else
                                    {
                                        // Lookup method in mixin
                                        if( !InvocationHandler.class.isAssignableFrom( mixinClass ) )
                                        {
                                            try
                                            {
                                                Method mixinMethod = mixinClass.getMethod( method.getName(), method.getParameterTypes() );
                                                if( helper.appliesTo( concern, mixinMethod, types, mixinClass ) )
                                                {
                                                    addConcernOrRepositionIfExists( concernsFor, helper.getConcernModel( concern ) );
                                                }
                                            }
                                            catch( NoSuchMethodException e )
                                            {
                                                // Ignore
                                            }
                                        }
                                    }
                                } );

        for( Annotation annotation : method.getAnnotations() )
        {
            if( annotation instanceof Concerns )
            {
                // Check @Concerns annotations directly on methods
                Concerns concerns = (Concerns) annotation;
                addConcerns( method, mixinClass, concernsFor, concerns );
            }
            else
            {
                // Check annotations on method that have @Concerns annotations themselves
                Concerns concerns = annotation.annotationType().getAnnotation( Concerns.class );
                addConcerns( method, mixinClass, concernsFor, concerns );
            }
        }

        if( concernsFor.isEmpty() )
        {
            return ConcernsModel.EMPTY_CONCERNS;
        }
        else
        {
            return new ConcernsModel( concernsFor );
        }
    }

    private void addConcerns( Method method, Class<?> mixinClass, List<ConcernModel> concernsFor, Concerns concerns )
    {
        if( concerns != null )
        {
            for( Class<?> concern : concerns.value() )
            {
                if( helper.appliesTo( concern, method, types, mixinClass ) )
                {
                    ConcernModel concernModel = helper.getConcernModel( concern );
                    addConcernOrRepositionIfExists( concernsFor, concernModel );
                }
            }
        }
    }

    private void addConcernOrRepositionIfExists( List<ConcernModel> concernsFor, ConcernModel concernModel )
    {
        // This remove/add is to allow re-ordering of the concerns
        concernsFor.remove( concernModel );
        concernsFor.add( concernModel );
    }

    private SideEffectsModel sideEffectsFor( Method method,
                                             Class<?> mixinClass,
                                             Stream<Class<?>> sideEffectClasses
                                           )
    {
        List<SideEffectModel> sideEffectsFor = new ArrayList<>();
        sideEffectClasses.forEach( sideEffect ->
                                   {
                                       SideEffectModel sideEffectModel = helper.getSideEffectModel( sideEffect );
                                       if( helper.appliesTo( sideEffect, method, types, mixinClass ) )
                                       {
                                           addSideEffectOrRepositionIfExists( sideEffectsFor, sideEffectModel );
                                       }
                                       else
                                       {
                                           // Lookup method in mixin
                                           if( !InvocationHandler.class.isAssignableFrom( mixinClass ) )
                                           {
                                               try
                                               {
                                                   Method mixinMethod = mixinClass.getMethod( method.getName(), method.getParameterTypes() );
                                                   if( helper.appliesTo( sideEffect, mixinMethod, types, mixinClass ) )
                                                   {
                                                       addSideEffectOrRepositionIfExists( sideEffectsFor, sideEffectModel );
                                                   }
                                               }
                                               catch( NoSuchMethodException e )
                                               {
                                                   // Ignore
                                               }
                                           }
                                       }
                                   } );

        for( Annotation annotation : method.getAnnotations() )
        {
            if( annotation instanceof SideEffects )
            {
                // Check SideEffects annotation on method
                SideEffects sideEffects = (SideEffects) annotation;
                addSideEffects( method, mixinClass, sideEffectsFor, sideEffects );
            }
            else
            {
                // Check annotations on method that have @SideEffects annotations themselves
                SideEffects sideEffects = annotation.annotationType().getAnnotation( SideEffects.class );
                addSideEffects( method, mixinClass, sideEffectsFor, sideEffects );
            }
        }

        if( sideEffectsFor.isEmpty() )
        {
            return SideEffectsModel.EMPTY_SIDEEFFECTS;
        }
        else
        {
            return new SideEffectsModel( sideEffectsFor );
        }
    }

    private void addSideEffects( Method method, Class<?> mixinClass, List<SideEffectModel> sideEffectsFor, SideEffects sideEffects )
    {
        if( sideEffects != null )
        {
            for( Class<?> sideEffect : sideEffects.value() )
            {
                if( helper.appliesTo( sideEffect, method, types, mixinClass ) )
                {
                    SideEffectModel sideEffectModel = helper.getSideEffectModel( sideEffect );
                    addSideEffectOrRepositionIfExists( sideEffectsFor, sideEffectModel );
                }
            }
        }
    }

    private void addSideEffectOrRepositionIfExists( List<SideEffectModel> sideEffectsFor,
                                                    SideEffectModel sideEffectModel
                                                  )
    {
        // This add/remove is to allow reording of SideEffects.
        sideEffectsFor.remove( sideEffectModel );
        sideEffectsFor.add( sideEffectModel );
    }

    @SuppressWarnings( "unchecked" )
    private Stream<Class<?>> constraintDeclarations( Class<?> type )
    {
        Stream<? extends Type> types = getTypes( type );
        return constraintDeclarations( types );
    }

    private Stream<Class<?>> constraintDeclarations( Stream<? extends Type> types )
    {
        return types
            .filter( mixinType -> Annotations.annotationOn( mixinType, Constraints.class ) != null )
            .flatMap( mixinType -> Arrays.stream( Annotations.annotationOn( mixinType, Constraints.class ).value() ) );
    }

    @SuppressWarnings( "unchecked" )
    private Stream<Class<?>> concernDeclarations( Class<?> type )
    {
        Stream<? extends Type> types = getTypes( type );
        return concernDeclarations( types );
    }

    private Stream<Class<?>> concernDeclarations( Stream<? extends Type> types )
    {
        return types
            .filter( mixinType -> Annotations.annotationOn( mixinType, Concerns.class ) != null )
            .flatMap( mixinType -> Arrays.stream( Annotations.annotationOn( mixinType, Concerns.class ).value() ) );
    }

    @SuppressWarnings( "unchecked" )
    private Stream<Class<?>> sideEffectDeclarations( Class<?> type )
    {
        Stream<? extends Type> types = getTypes( type );
        return sideEffectDeclarations( types );
    }

    private Stream<Class<?>> sideEffectDeclarations( Stream<? extends Type> types )
    {
        return types
            .filter( mixinType -> Annotations.annotationOn( mixinType, SideEffects.class ) != null )
            .flatMap( mixinType -> Arrays.stream( Annotations.annotationOn( mixinType, SideEffects.class ).value() ) );
    }

    private Stream<Class<?>> mixinDeclarations( Class<?> type )
    {
        //Stream<? extends Type> types = typesOf( type );
        return mixinDeclarations( of( type ) );
    }

    private Stream<Class<?>> mixinDeclarations( Stream<? extends Class> types )
    {
        return types.flatMap( this::getTypes ).flatMap( Classes::typesOf )
                    .filter( mixinType -> Annotations.annotationOn( mixinType, Mixins.class ) != null )
                    .flatMap( mixinType -> Arrays.stream( Annotations.annotationOn( mixinType, Mixins.class ).value() ) );
    }

    private Stream<Class> getAllTypes()
    {
        return this.types.stream().flatMap( this::getTypes );
    }

    private Stream<Class> getTypes( Class<?> clazz )
    {
        if( clazz.isInterface() )
        {
            return typesOf( clazz ).map( Classes.RAW_CLASS );
        }
        else
        {
            return classHierarchy( clazz ).map( Classes.RAW_CLASS );
        }
    }

    private AssociationModel newAssociationModel(AccessibleObject accessor,
                                                 List<Class<?>> constraintClasses
                                                )
    {
        List<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn( accessor );
        boolean optional = annotations.stream().anyMatch( isType( Optional.class ) );

        // Constraints for Association references
        ValueConstraintsModel constraintsModel =
            constraintsFor( annotations.stream(),
                            GenericAssociationInfo.associationTypeOf( accessor ),
                            ( (Member) accessor ).getName(),
                            optional,
                            constraintClasses,
                            accessor );
        ValueConstraintsInstance valueConstraintsInstance = constraintsModel.newInstance();

        // Constraints for the Association itself
        constraintsModel = constraintsFor( annotations.stream(),
                                           Association.class,
                                           ( (Member) accessor ).getName(),
                                           optional,
                                           constraintClasses,
                                           accessor );
        ValueConstraintsInstance associationValueConstraintsInstance = constraintsModel.newInstance();

        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        return new AssociationModel( accessor, valueConstraintsInstance, associationValueConstraintsInstance, metaInfo );
    }

    private ManyAssociationModel newManyAssociationModel(AccessibleObject accessor,
                                                         List<Class<?>> constraintClasses
                                                        )
    {
        List<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn( accessor );
        boolean optional = annotations.stream().anyMatch( isType( Optional.class ) );

        // Constraints for entities in ManyAssociation
        ValueConstraintsModel valueConstraintsModel =
            constraintsFor( annotations.stream(),
                            GenericAssociationInfo.associationTypeOf( accessor ),
                            ( (Member) accessor ).getName(),
                            optional,
                            constraintClasses,
                            accessor );
        ValueConstraintsInstance valueConstraintsInstance = valueConstraintsModel.newInstance();

        // Constraints for the ManyAssociation itself
        valueConstraintsModel = constraintsFor( annotations.stream(),
                                                ManyAssociation.class,
                                                ( (Member) accessor ).getName(),
                                                optional,
                                                constraintClasses,
                                                accessor );
        ValueConstraintsInstance manyValueConstraintsInstance = valueConstraintsModel.newInstance();

        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        return new ManyAssociationModel( accessor, valueConstraintsInstance, manyValueConstraintsInstance, metaInfo );
    }

    private NamedAssociationModel newNamedAssociationModel( AccessibleObject accessor,
                                                            List<Class<?>> constraintClasses
                                                          )
    {
        List<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn( accessor );
        boolean optional = annotations.stream().anyMatch( isType( Optional.class ) );

        // Constraints for entities in NamedAssociation
        ValueConstraintsModel valueConstraintsModel =
            constraintsFor( annotations.stream(),
                            GenericAssociationInfo.associationTypeOf( accessor ),
                            ( (Member) accessor ).getName(),
                            optional,
                            constraintClasses,
                            accessor );
        ValueConstraintsInstance valueConstraintsInstance = valueConstraintsModel.newInstance();

        // Constraints for the NamedAssociation itself
        valueConstraintsModel = constraintsFor( annotations.stream(),
                                                NamedAssociation.class,
                                                ( (Member) accessor ).getName(),
                                                optional,
                                                constraintClasses,
                                                accessor );
        ValueConstraintsInstance namedValueConstraintsInstance = valueConstraintsModel.newInstance();

        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        return new NamedAssociationModel( accessor, valueConstraintsInstance, namedValueConstraintsInstance, metaInfo );
    }
}
