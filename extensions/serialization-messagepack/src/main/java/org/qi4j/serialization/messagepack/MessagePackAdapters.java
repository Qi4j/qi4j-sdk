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
 */
package org.qi4j.serialization.messagepack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.serialization.Converters;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.type.ValueType;
import org.qi4j.spi.serialization.BuiltInConverters;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;

import static org.qi4j.api.type.HasTypesCollectors.closestType;
import static org.qi4j.serialization.messagepack.MessagePackSettings.orDefault;

@Mixins( MessagePackAdapters.Mixin.class )
public interface MessagePackAdapters
{
    void registerAdapter( ValueType valueType, MessagePackAdapter<?> adapter );

    <T> MessagePackAdapter<T> adapterFor( ValueType valueType );

    default <T> MessagePackAdapter<T> adapterFor( Class<T> type )
    {
        return adapterFor( ValueType.of( type ) );
    }

    class Mixin implements MessagePackAdapters, Initializable
    {
        private Map<ValueType, MessagePackAdapter<?>> adapters = new LinkedHashMap<>();

        @Uses
        private ServiceDescriptor descriptor;

        @This
        private BuiltInConverters builtInConverters;

        @This
        private Converters converters;

        @Override
        public void initialize()
        {
            MessagePackSettings settings = orDefault( descriptor.metaInfo( MessagePackSettings.class ) );
            settings.getConverters()
                    .forEach( ( type, converter ) -> converters.registerConverter( type, converter ) );
            builtInConverters.registerBuiltInConverters( converters );
            settings.getAdapters().forEach( adapters::put );
            registerBaseMessagePackAdapters();
        }

        @Override
        public void registerAdapter( ValueType valueType, MessagePackAdapter<?> adapter )
        {
            adapters.put( valueType, adapter );
        }

        @Override
        public <T> MessagePackAdapter<T> adapterFor( final ValueType valueType )
        {
            return castAdapter( adapters.keySet().stream()
                                        .collect( closestType( valueType ) )
                                        .map( adapters::get )
                                        .orElse( null ) );
        }

        @SuppressWarnings( "unchecked" )
        private <T> MessagePackAdapter<T> castAdapter( MessagePackAdapter<?> adapter )
        {
            return (MessagePackAdapter<T>) adapter;
        }

        private void registerBaseMessagePackAdapters()
        {
            // Primitive Value types
            adapters.put( ValueType.STRING, new StringAdapter() );
            adapters.put( ValueType.CHARACTER, new CharacterAdapter() );
            adapters.put( ValueType.BOOLEAN, new BooleanAdapter() );
            adapters.put( ValueType.INTEGER, new IntegerAdapter() );
            adapters.put( ValueType.LONG, new LongAdapter() );
            adapters.put( ValueType.SHORT, new ShortAdapter() );
            adapters.put( ValueType.BYTE, new ByteAdapter() );
            adapters.put( ValueType.FLOAT, new FloatAdapter() );
            adapters.put( ValueType.DOUBLE, new DoubleAdapter() );
        }

        private static abstract class ToStringAdapter<T> implements MessagePackAdapter<T>
        {
            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newString( object.toString() );
            }
        }

        private static class StringAdapter extends ToStringAdapter<String>
        {
            @Override
            public Class<String> type() { return String.class; }

            @Override
            public String deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asStringValue().asString();
            }
        }

        private static class CharacterAdapter extends ToStringAdapter<Character>
        {
            @Override
            public Class<Character> type() { return Character.class; }

            @Override
            public Character deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                String string = value.asStringValue().asString();
                return string.isEmpty() ? null : string.charAt( 0 );
            }
        }

        private static class BooleanAdapter implements MessagePackAdapter<Boolean>
        {
            @Override
            public Class<Boolean> type() { return Boolean.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newBoolean( (Boolean) object );
            }

            @Override
            public Boolean deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asBooleanValue().getBoolean();
            }
        }

        private static class IntegerAdapter implements MessagePackAdapter<Integer>
        {
            @Override
            public Class<Integer> type() { return Integer.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newInteger( (Integer) object );
            }

            @Override
            public Integer deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asIntegerValue().asInt();
            }
        }

        private static class LongAdapter implements MessagePackAdapter<Long>
        {
            @Override
            public Class<Long> type() { return Long.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newInteger( (Long) object );
            }

            @Override
            public Long deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asIntegerValue().asLong();
            }
        }

        private static class ShortAdapter implements MessagePackAdapter<Short>
        {
            @Override
            public Class<Short> type() { return Short.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newInteger( (Short) object );
            }

            @Override
            public Short deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asIntegerValue().asShort();
            }
        }

        private static class ByteAdapter implements MessagePackAdapter<Byte>
        {
            @Override
            public Class<Byte> type() { return Byte.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newInteger( (Byte) object );
            }

            @Override
            public Byte deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asIntegerValue().asByte();
            }
        }

        private static class FloatAdapter implements MessagePackAdapter<Float>
        {
            @Override
            public Class<Float> type() { return Float.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newFloat( (Float) object );
            }

            @Override
            public Float deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asFloatValue().toFloat();
            }
        }

        private static class DoubleAdapter implements MessagePackAdapter<Double>
        {
            @Override
            public Class<Double> type() { return Double.class; }

            @Override
            public Value serialize( Object object, Function<Object, Value> serialize )
            {
                return ValueFactory.newFloat( (Double) object );
            }

            @Override
            public Double deserialize( Value value, BiFunction<Value, ValueType, Object> deserialize )
            {
                return value.asFloatValue().toDouble();
            }
        }
    }
}
