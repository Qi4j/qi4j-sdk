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

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Stream;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.bootstrap.ObjectAssembly;
import org.qi4j.runtime.object.ObjectModel;
import org.qi4j.bootstrap.ObjectAssembly;

/**
 * Assembly of an Object.
 */
public final class ObjectAssemblyImpl
    implements ObjectAssembly
{
    private Class<?> objectType;
    MetaInfo metaInfo = new MetaInfo();
    Visibility visibility = Visibility.module;

    public ObjectAssemblyImpl( Class<?> clazz )
    {
        // best try to find out if the class is a concrete class
        if( clazz.isEnum() ||
            ( !Composite.class.isAssignableFrom( clazz ) && Modifier.isAbstract( clazz.getModifiers() ) ) )
        {
            throw new IllegalArgumentException( "Declared objects must be concrete classes: " + clazz );
        }
        this.objectType = clazz;
    }

    @Override
    public Stream<Class<?>> types()
    {
        return Stream.of( objectType );
    }

    void addObjectModel( ModuleDescriptor module, List<ObjectModel> objectModels )
    {
        try
        {
            ObjectModel objectModel = new ObjectModel( module, objectType, visibility, metaInfo );
            objectModels.add( objectModel );
        }
        catch( Throwable e )
        {
            throw new InvalidApplicationException( "Could not register " + objectType.getName(), e );
        }
    }
}
