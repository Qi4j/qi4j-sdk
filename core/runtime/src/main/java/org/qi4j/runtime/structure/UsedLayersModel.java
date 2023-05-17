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
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.api.structure.UsedLayersDescriptor;
import org.qi4j.api.util.HierarchicalVisitor;
import org.qi4j.api.util.VisitableHierarchy;

/**
 * JAVADOC
 */
public final class UsedLayersModel
    implements UsedLayersDescriptor, VisitableHierarchy<Object, Object>
{
    private final List<LayerModel> usedLayers;

    public UsedLayersModel( List<LayerModel> usedLayers )
    {
        this.usedLayers = usedLayers;
    }

    @Override
    public Stream<? extends LayerDescriptor> layers()
    {
        return usedLayers.stream();
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( LayerModel usedLayer : usedLayers )
            {
                if( !usedLayer.accept( visitor ) )
                {
                    break;
                }
            }
        }

        return visitor.visitLeave( this );
    }

    public UsedLayersInstance newInstance( List<LayerDescriptor> usedLayerInstances )
    {
        return new UsedLayersInstance( usedLayerInstances );
    }

    @Override
    public String toString()
    {
        return usedLayers.toString();
    }
}
