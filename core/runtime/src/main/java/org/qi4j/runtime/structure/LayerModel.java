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

import java.util.List;
import java.util.stream.Stream;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.ModelDescriptor;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.util.HierarchicalVisitor;
import org.qi4j.api.util.VisitableHierarchy;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.runtime.activation.ActivatorsInstance;
import org.qi4j.runtime.activation.ActivatorsModel;
import org.qi4j.runtime.activation.ActivatorsInstance;
import org.qi4j.runtime.activation.ActivatorsModel;

/**
 * JAVADOC
 */
public final class LayerModel
    implements LayerDescriptor, VisitableHierarchy<Object, Object>
{
    // Model
    private final String name;
    private final MetaInfo metaInfo;
    private final UsedLayersModel usedLayersModel;
    private final ActivatorsModel<Layer> activatorsModel;
    private final List<ModuleModel> modules;
    private LayerInstance layerInstance;

    public LayerModel( String name,
                       MetaInfo metaInfo,
                       UsedLayersModel usedLayersModel,
                       ActivatorsModel<Layer> activatorsModel,
                       List<ModuleModel> modules
    )
    {
        this.name = name;
        this.metaInfo = metaInfo;
        this.usedLayersModel = usedLayersModel;
        this.activatorsModel = activatorsModel;
        this.modules = modules;
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

    public Stream<? extends ModuleDescriptor> modules()
    {
        return modules.stream();
    }

    @Override
    public UsedLayersModel usedLayers()
    {
        return usedLayersModel;
    }

    public ActivatorsInstance<Layer> newActivatorsInstance()
        throws ActivationException
    {
        return new ActivatorsInstance<>( activatorsModel.newInstances() );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> modelVisitor )
        throws ThrowableType
    {
        if( modelVisitor.visitEnter( this ) )
        {
            if( activatorsModel.accept( modelVisitor ) )
            {
                for( ModuleModel module : modules )
                {
                    if( !module.accept( modelVisitor ) )
                    {
                        break;
                    }
                }
            }
        }
        return modelVisitor.visitLeave( this );
    }

    @Override
    public Layer instance()
    {
        return layerInstance;
    }

    public LayerInstance newInstance( ApplicationInstance applicationInstance )
    {
        layerInstance = new LayerInstance( this, applicationInstance );
        for( ModuleModel module : modules )
        {
            ModuleInstance moduleInstance = module.newInstance( this );
            layerInstance.addModule( moduleInstance );
        }
        return layerInstance;
    }

    @Override
    public Stream<? extends ObjectDescriptor> visibleObjects( final Visibility visibility )
    {
        return modules.stream().flatMap( module -> module.visibleObjects( visibility ) );
    }

    @Override
    public Stream<? extends TransientDescriptor> visibleTransients( final Visibility visibility )
    {
        return modules.stream().flatMap( module -> module.visibleTransients( visibility ) );
    }

    @Override
    public Stream<? extends EntityDescriptor> visibleEntities( final Visibility visibility )
    {
        return modules.stream().flatMap( module -> module.visibleEntities( visibility ) );
    }

    @Override
    public Stream<? extends ValueDescriptor> visibleValues( final Visibility visibility )
    {
        return modules.stream().flatMap( module -> module.visibleValues( visibility ) );
    }

    @Override
    public Stream<? extends ModelDescriptor> visibleServices( final Visibility visibility )
    {
        return modules.stream().flatMap( module -> module.visibleServices( visibility ) );
    }


    @Override
    public String toString()
    {
        return name;
    }
}
