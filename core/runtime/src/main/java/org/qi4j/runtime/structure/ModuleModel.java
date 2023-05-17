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
package org.qi4j.runtime.structure;

import java.util.stream.Stream;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.structure.TypeLookup;
import org.qi4j.api.util.HierarchicalVisitor;
import org.qi4j.api.util.VisitableHierarchy;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.runtime.activation.ActivatorsInstance;
import org.qi4j.runtime.activation.ActivatorsModel;
import org.qi4j.runtime.composite.TransientsModel;
import org.qi4j.runtime.entity.EntitiesModel;
import org.qi4j.runtime.object.ObjectsModel;
import org.qi4j.runtime.service.ImportedServicesModel;
import org.qi4j.runtime.service.ServicesModel;
import org.qi4j.runtime.value.ValuesModel;
import org.qi4j.runtime.activation.ActivatorsInstance;
import org.qi4j.runtime.activation.ActivatorsModel;

import static java.util.stream.Stream.concat;
import static org.qi4j.api.common.Visibility.application;
import static org.qi4j.api.common.Visibility.layer;
import static org.qi4j.api.common.Visibility.module;

/**
 * JAVADOC
 */
public class ModuleModel
    implements ModuleDescriptor, VisitableHierarchy<Object, Object>
{
    private final LayerDescriptor layerModel;
    private final ActivatorsModel<Module> activatorsModel;
    private final TransientsModel transientsModel;
    private final EntitiesModel entitiesModel;
    private final ObjectsModel objectsModel;
    private final ValuesModel valuesModel;
    private final ServicesModel servicesModel;
    private final ImportedServicesModel importedServicesModel;
    private final TypeLookupImpl typeLookup;
    private final ClassLoader classLoader;

    private final String name;
    private final MetaInfo metaInfo;
    private ModuleInstance moduleInstance;

    public ModuleModel( String name,
                        MetaInfo metaInfo,
                        LayerDescriptor layerModel,
                        ActivatorsModel<Module> activatorsModel,
                        TransientsModel transientsModel,
                        EntitiesModel entitiesModel,
                        ObjectsModel objectsModel,
                        ValuesModel valuesModel,
                        ServicesModel servicesModel,
                        ImportedServicesModel importedServicesModel
    )
    {
        this.name = name;
        this.metaInfo = metaInfo;
        this.layerModel = layerModel;
        this.activatorsModel = activatorsModel;
        this.transientsModel = transientsModel;
        this.entitiesModel = entitiesModel;
        this.objectsModel = objectsModel;
        this.valuesModel = valuesModel;
        this.servicesModel = servicesModel;
        this.importedServicesModel = importedServicesModel;
        typeLookup = new TypeLookupImpl( this );
        classLoader = new ModuleClassLoader( this, Thread.currentThread().getContextClassLoader() );
    }

    @Override
    public String name()
    {
        return name;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    @Override
    public LayerDescriptor layer()
    {
        return layerModel;
    }

    @Override
    public ClassLoader classLoader()
    {
        return classLoader;
    }

    public ActivatorsInstance<Module> newActivatorsInstance()
        throws ActivationException
    {
        return new ActivatorsInstance<>( activatorsModel.newInstances() );
    }

    @Override
    public EntityDescriptor entityDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            EntityDescriptor entityModel = typeLookup.lookupEntityModel( type );
            if( entityModel == null )
            {
                return null;
            }
            return entityModel;
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public ObjectDescriptor objectDescriptor( String typeName )
    {
        try
        {
            Class<?> type = classLoader().loadClass( typeName );
            ObjectDescriptor objectModel = typeLookup.lookupObjectModel( type );
            if( objectModel == null )
            {
                return null;
            }
            return objectModel;
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public TransientDescriptor transientDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            TransientDescriptor transientModel = typeLookup.lookupTransientModel( type );
            if( transientModel == null )
            {
                return null;
            }
            return transientModel;
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public ValueDescriptor valueDescriptor( String name )
    {
        try
        {
            Class<?> type = classLoader().loadClass( name );
            ValueDescriptor valueModel = typeLookup.lookupValueModel( type );
            if( valueModel == null )
            {
                return null;
            }
            return valueModel;
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    @Override
    public Module instance()
    {
        return moduleInstance;
    }

    @Override
    public TypeLookup typeLookup()
    {
        return typeLookup;
    }

    public ModuleInstance newInstance( LayerDescriptor layerInstance )
    {
        moduleInstance = new ModuleInstance( this, layerInstance, typeLookup, servicesModel, importedServicesModel );
        return moduleInstance;
    }

    @Override
    public Stream<? extends TransientDescriptor> transientComposites()
    {
        return transientsModel.stream();
    }

    @Override
    public Stream<? extends ValueDescriptor> valueComposites()
    {
        return valuesModel.stream();
    }

    @Override
    public Stream<? extends ServiceDescriptor> serviceComposites()
    {
        return servicesModel.stream();
    }

    @Override
    public Stream<? extends EntityDescriptor> entityComposites()
    {
        return entitiesModel.stream();
    }

    @Override
    public Stream<? extends ImportedServiceDescriptor> importedServices()
    {
        return importedServicesModel.stream();
    }

    @Override
    public Stream<? extends ObjectDescriptor> objects()
    {
        return objectsModel.stream();
    }

    @Override
    public Stream<? extends ValueDescriptor> findVisibleValueTypes()
    {
        return concat( visibleValues( module ),
                       concat(
                           layer().visibleValues( layer ),
                           concat(
                               layer().visibleValues( application ),
                               layer().usedLayers().layers().flatMap( layer1 -> layer1.visibleValues( application ) )
                           )
                       )
        );
    }

    @Override
    public Stream<? extends EntityDescriptor> findVisibleEntityTypes()
    {
        return typeLookup.allEntities();
    }

    @Override
    public Stream<? extends TransientDescriptor> findVisibleTransientTypes()
    {
        return typeLookup.allTransients();
    }

    @Override
    public Stream<? extends ObjectDescriptor> findVisibleObjectTypes()
    {
        return typeLookup.allObjects();
    }

    public Stream<? extends ObjectDescriptor> visibleObjects( Visibility visibility )
    {
        return objectsModel.stream()
            .filter( new VisibilityPredicate( visibility ) );
    }

    public Stream<? extends TransientDescriptor> visibleTransients( Visibility visibility )
    {
        return transientsModel.stream()
            .filter( new VisibilityPredicate( visibility ) );
    }

    public Stream<? extends EntityDescriptor> visibleEntities( Visibility visibility )
    {
        return entitiesModel.stream()
            .filter( new VisibilityPredicate( visibility ) );
    }

    public Stream<? extends ValueDescriptor> visibleValues( Visibility visibility )
    {
        return valuesModel.stream()
            .filter( new VisibilityPredicate( visibility ) );
    }

    public Stream<? extends ModelDescriptor> visibleServices( Visibility visibility )
    {
        return concat(
            servicesModel.stream()
                .filter( new VisibilityPredicate( visibility ) ),
            importedServicesModel.stream()
                .filter( new VisibilityPredicate( visibility ) )
        );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor )
        throws ThrowableType
    {
        if( modelVisitor.visitEnter( this ) )
        {
            if( activatorsModel.accept( modelVisitor ) )
            {
                if( transientsModel.accept( modelVisitor ) )
                {
                    if( entitiesModel.accept( modelVisitor ) )
                    {
                        if( servicesModel.accept( modelVisitor ) )
                        {
                            if( importedServicesModel.accept( modelVisitor ) )
                            {
                                if( objectsModel.accept( modelVisitor ) )
                                {
                                    valuesModel.accept( modelVisitor );
                                }
                            }
                        }
                    }
                }
            }
        }
        return modelVisitor.visitLeave( this );
    }

    @Override
    public String toString()
    {
        return name;
    }
}
