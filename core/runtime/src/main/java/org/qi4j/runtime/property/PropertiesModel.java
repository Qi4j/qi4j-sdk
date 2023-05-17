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
package org.qi4j.runtime.property;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.util.HierarchicalVisitor;
import org.qi4j.api.util.VisitableHierarchy;

/**
 * Base class for properties model
 */
public class PropertiesModel
    implements VisitableHierarchy<Object, Object>
{
    private final Map<AccessibleObject, PropertyModel> mapAccessiblePropertyModel = new LinkedHashMap<>();
    private final Map<QualifiedName, PropertyModel> mapNamePropertyModel = new LinkedHashMap<>();

    public PropertiesModel()
    {
    }

    public void addProperty( PropertyModel property )
    {
        mapAccessiblePropertyModel.put( property.accessor(), property );
        mapNamePropertyModel.put( property.qualifiedName(), property );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            for( PropertyModel propertyModel : mapAccessiblePropertyModel.values() )
            {
                if( !propertyModel.accept( visitor ) )
                {
                    break;
                }
            }
        }

        return visitor.visitLeave( this );
    }

    public Stream<PropertyModel> properties()
    {
        return mapAccessiblePropertyModel.values().stream();
    }

    public PropertyModel getProperty( AccessibleObject accessor )
    {
        PropertyModel propertyModel = mapAccessiblePropertyModel.get( accessor );
        if( propertyModel == null )
        {
            throw new IllegalArgumentException( "No property found with name: " + ( (Member) accessor ).getName() );
        }
        return propertyModel;
    }

    public PropertyModel getPropertyByName( String name )
        throws IllegalArgumentException
    {
        for( PropertyModel propertyModel : mapAccessiblePropertyModel.values() )
        {
            if( propertyModel.qualifiedName().name().equals( name ) )
            {
                return propertyModel;
            }
        }
        throw new IllegalArgumentException( "No property found with name: " + name );
    }

    public PropertyModel getPropertyByQualifiedName( QualifiedName name )
        throws IllegalArgumentException
    {
        PropertyModel propertyModel = mapNamePropertyModel.get( name );
        if( propertyModel != null )
        {
            return propertyModel;
        }
        throw new IllegalArgumentException( "No property found with qualified name: " + name );
    }

    public boolean hasProperty( QualifiedName name )
    {
        return mapNamePropertyModel.containsKey( name );
    }
}
