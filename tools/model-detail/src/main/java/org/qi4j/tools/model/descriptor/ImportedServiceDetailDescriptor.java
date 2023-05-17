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
package org.qi4j.tools.model.descriptor;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import org.qi4j.api.util.HierarchicalVisitor;
import org.qi4j.api.util.VisitableHierarchy;

/**
 * Imported Service Detail Descriptor.
 * <p>
 * Visitable hierarchy with Activators children.
 */
// TODO need to refactor later, but wait until Qi4j core/spi have proper and consistent API for ImportedService.
public class ImportedServiceDetailDescriptor
    extends CompositeDetailDescriptor<ImportedServiceCompositeDescriptor>
    implements ActivateeDetailDescriptor, VisitableHierarchy<Object, Object>
{
    private final List<ActivatorDetailDescriptor> activators = new LinkedList<>();

    ImportedServiceDetailDescriptor( ImportedServiceCompositeDescriptor descriptor )
    {
        super( descriptor );
    }

    @Override
    public Iterable<ActivatorDetailDescriptor> activators()
    {
        return activators;
    }

    final void addActivator( ActivatorDetailDescriptor descriptor )
    {
        Objects.requireNonNull( descriptor, "ActivatorDetailDescriptor" );
        descriptor.setImportedService( this );
        activators.add( descriptor );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( ActivatorDetailDescriptor activator : activators )
            {
                if( !activator.accept( visitor ) )
                {
                    break;
                }
            }
        }
        return visitor.visitLeave( this );
    }

    public JsonObjectBuilder toJson()
    {
        JsonObjectBuilder builder = super.toJson();
        {
            JsonArrayBuilder activatorsBuilder = Json.createArrayBuilder();
            activators().forEach( activator -> activatorsBuilder.add( activator.toJson() ) );
            builder.add( "activators", activatorsBuilder.build() );
        }
        return builder;
    }
}
