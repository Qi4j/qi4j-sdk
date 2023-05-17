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

package org.qi4j.runtime.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.type.ArrayType;
import org.qi4j.api.type.CollectionType;
import org.qi4j.api.type.EnumType;
import org.qi4j.api.type.MapType;
import org.qi4j.api.type.ValueCompositeType;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.util.Classes;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.value.ValueInstance;
import org.qi4j.spi.type.ValueTypeFactory;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.value.ValueInstance;

import static org.qi4j.api.composite.CompositeInstance.compositeInstanceOf;

public class ValueTypeFactoryInstance implements ValueTypeFactory
{
    private static final ValueTypeFactoryInstance INSTANCE = new ValueTypeFactoryInstance();

    public static ValueTypeFactoryInstance instance()
    {
        return INSTANCE;
    }

    @Override
    public ValueType valueTypeOf( ModuleDescriptor module, Object object )
    {
        if( object instanceof ValueComposite )
        {
            ValueInstance valueInstance = (ValueInstance) compositeInstanceOf( (Composite) object );
            return valueInstance.descriptor().valueType();
        }
        if( object instanceof EntityComposite )
        {
            return ( (EntityInstance) compositeInstanceOf( (Composite) object ) ).descriptor().valueType();
        }
        if( object instanceof Enum )
        {
            return EnumType.of( ( (Enum) object ).getDeclaringClass() );
        }
        return valueTypeOf( module, object.getClass() );
    }

    @Override
    public ValueType valueTypeOf( ModuleDescriptor module, Class<?> type )
    {
        ValueDescriptor valueDescriptor = module.typeLookup().lookupValueModel( type );
        if( valueDescriptor != null )
        {
            return valueDescriptor.valueType();
        }
        EntityDescriptor entityDescriptor = module.typeLookup().lookupEntityModel( type );
        if( entityDescriptor != null )
        {
            return entityDescriptor.valueType();
        }
        return newValueType( type, type, type, module );
    }

    public ValueType newValueType( Type type, Class declaringClass, Class compositeType, ModuleDescriptor module )
    {
        ValueType valueType;
        if( EnumType.isEnum( type ) )
        {
            valueType = EnumType.of( Classes.RAW_CLASS.apply( type ) );
        }
        else if( ArrayType.isArray( type ) )
        {
            valueType = ArrayType.of( Classes.RAW_CLASS.apply( type ) );
        }
        else if( CollectionType.isCollection( type ) )
        {
            if( type instanceof ParameterizedType )
            {
                ParameterizedType pt = (ParameterizedType) type;
                Type collectionType = pt.getActualTypeArguments()[ 0 ];
                if( collectionType instanceof TypeVariable && declaringClass != null )
                {
                    TypeVariable collectionTypeVariable = (TypeVariable) collectionType;
                    collectionType = Classes.resolveTypeVariable( collectionTypeVariable, declaringClass,
                                                                  compositeType );
                }
                ValueType collectedType = newValueType( collectionType, declaringClass, compositeType, module );
                valueType = CollectionType.of( Classes.RAW_CLASS.apply( type ), collectedType );
            }
            else
            {
                ValueType collectedType = newValueType( Object.class, declaringClass, compositeType, module );
                valueType = CollectionType.of( Classes.RAW_CLASS.apply( type ), collectedType );
            }
        }
        else if( MapType.isMap( type ) )
        {
            if( type instanceof ParameterizedType )
            {
                ParameterizedType pt = (ParameterizedType) type;
                Type keyType = pt.getActualTypeArguments()[ 0 ];
                if( keyType instanceof TypeVariable && declaringClass != null )
                {
                    TypeVariable keyTypeVariable = (TypeVariable) keyType;
                    keyType = Classes.resolveTypeVariable( keyTypeVariable, declaringClass, compositeType );
                }
                ValueType keyedType = newValueType( keyType, declaringClass, compositeType, module );
                Type valType = pt.getActualTypeArguments()[ 1 ];
                if( valType instanceof TypeVariable && declaringClass != null )
                {
                    TypeVariable valueTypeVariable = (TypeVariable) valType;
                    valType = Classes.resolveTypeVariable( valueTypeVariable, declaringClass, compositeType );
                }
                ValueType valuedType = newValueType( valType, declaringClass, compositeType, module );
                valueType = MapType.of( Classes.RAW_CLASS.apply( type ), keyedType, valuedType );
            }
            else
            {
                ValueType keyType = newValueType( Object.class, declaringClass, compositeType, module );
                ValueType valuesType = newValueType( Object.class, declaringClass, compositeType, module );
                valueType = MapType.of( Classes.RAW_CLASS.apply( type ), keyType, valuesType );
            }
        }
        else if( ValueCompositeType.isValueComposite( type ) )
        {
            ValueDescriptor model = module.typeLookup().lookupValueModel( Classes.RAW_CLASS.apply( type ) );
            if( model == null )
            {
                throw new InvalidApplicationException(
                    "[" + module.name() + "] Could not find ValueComposite of type " + type );
            }

            valueType = model.valueType();
        }
        else
        {
            valueType = ValueType.of( Classes.RAW_CLASS.apply( type ) );
        }

        return valueType;
    }
}
