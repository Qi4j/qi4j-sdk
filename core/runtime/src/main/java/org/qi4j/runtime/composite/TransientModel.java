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

package org.qi4j.runtime.composite;

import java.util.List;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.property.PropertyModel;

/**
 * Model for Transient Composites
 */
public class TransientModel extends CompositeModel
    implements TransientDescriptor
{
    public TransientModel( ModuleDescriptor module,
                           List<Class<?>> types, final Visibility visibility,
                           final MetaInfo metaInfo,
                           final MixinsModel mixinsModel,
                           final StateModel stateModel,
                           final CompositeMethodsModel compositeMethodsModel
    )
    {
        super( module, types, visibility, metaInfo, mixinsModel, stateModel, compositeMethodsModel );
    }

    public TransientInstance newInstance( UsesInstance uses,
                                          TransientStateInstance state
    )
    {
        Object[] mixins = mixinsModel.newMixinHolder();
        TransientInstance compositeInstance = new TransientInstance( this, mixins, state );

        // Instantiate all mixins
        int i = 0;
        InjectionContext injectionContext = new InjectionContext( compositeInstance, uses, state );
        for( MixinModel mixinModel : mixinsModel.mixinModels() )
        {
            mixins[ i++ ] = mixinModel.newInstance( injectionContext );
        }

        // Return
        return compositeInstance;
    }

    public void checkConstraints( TransientStateInstance instanceState )
        throws ConstraintViolationException
    {
        stateModel.properties().forEach( ( PropertyModel propertyModel ) ->
                                         {
                                             try
                                             {
                                                 propertyModel.checkConstraints( instanceState.propertyFor( propertyModel.accessor() ).get() );
                                             }
                                             catch( ConstraintViolationException e )
                                             {
                                                 e.setCompositeDescriptor( this );
                                                 throw e;
                                             }
                                         }
        );
    }
}
