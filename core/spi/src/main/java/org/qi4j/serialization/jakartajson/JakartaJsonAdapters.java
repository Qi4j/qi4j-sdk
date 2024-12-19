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
package org.qi4j.serialization.jakartajson;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import jakarta.json.JsonNumber;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.serialization.Converters;
import org.qi4j.api.serialization.SerializationException;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.type.ValueType;
import org.qi4j.spi.serialization.BuiltInConverters;

import static org.qi4j.api.type.HasTypesCollectors.closestType;
import static org.qi4j.serialization.jakartajson.JakartaJsonSettings.orDefault;

@Mixins( JakartaJsonAdapters.Mixin.class )
public interface JakartaJsonAdapters
{
    void registerAdapter( ValueType valueType, JakartaJsonAdapter<?> adapter );

    <T> JakartaJsonAdapter<T> adapterFor(ValueType valueType );

    default <T> JakartaJsonAdapter<T> adapterFor(Class<T> type )
    {
        return adapterFor( ValueType.of( type ) );
    }

    class Mixin implements JakartaJsonAdapters, Initializable
    {
        private final Map<ValueType, JakartaJsonAdapter<?>> adapters = new LinkedHashMap<>();
        private final Map<ValueType, JakartaJsonAdapter<?>> resolvedAdaptersCache = new HashMap<>();

        @Uses
        private ServiceDescriptor descriptor;

        @This
        private BuiltInConverters builtInConverters;

        @This
        private Converters converters;

        @Override
        public void initialize()
        {
            JakartaJsonSettings settings = orDefault( descriptor.metaInfo( JakartaJsonSettings.class ) );
            settings.getConverters()
                    .forEach( ( type, converter ) -> converters.registerConverter( type, converter ) );
            builtInConverters.registerBuiltInConverters( converters );
            settings.getAdapters().forEach( adapters::put );
            registerBaseJakartaJsonAdapters();
        }

        @Override
        public void registerAdapter( ValueType valueType, JakartaJsonAdapter<?> adapter )
        {
            adapters.put( valueType, adapter );
            resolvedAdaptersCache.put( valueType, adapter );
        }

        @Override
        public <T> JakartaJsonAdapter<T> adapterFor(ValueType valueType )
        {
            if( resolvedAdaptersCache.containsKey( valueType ) )
            {
                return castAdapter( resolvedAdaptersCache.get( valueType ) );
            }
            JakartaJsonAdapter<T> adapter = castAdapter( adapters.keySet().stream()
                                                               .collect( closestType( valueType ) )
                                                               .map( adapters::get )
                                                               .orElse( null ) );
            resolvedAdaptersCache.put( valueType, adapter );
            return adapter;
        }

        @SuppressWarnings( "unchecked" )
        private <T> JakartaJsonAdapter<T> castAdapter(JakartaJsonAdapter<?> adapter )
        {
            return (JakartaJsonAdapter<T>) adapter;
        }

        private void registerBaseJakartaJsonAdapters()
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

        private static abstract class ToJsonStringAdapter<T> implements JakartaJsonAdapter<T>
        {
            @Override
            public JsonValue serialize(JakartaJsonFactories jsonFactories, T object,
                                       Function<Object, JsonValue> serialize )
            {
                return jsonFactories.toJsonString( object );
            }
        }

        private static class StringAdapter extends ToJsonStringAdapter<String>
        {
            @Override
            public Class<String> type() { return String.class; }

            @Override
            public String deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserialize )
            {
                return JakartaJson.asString( json );
            }
        }

        private static class CharacterAdapter extends ToJsonStringAdapter<Character>
        {
            @Override
            public Class<Character> type() { return Character.class; }

            @Override
            public Character deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserialize )
            {
                String string = JakartaJson.asString( json );
                return string.isEmpty() ? null : string.charAt( 0 );
            }
        }

        private static class BooleanAdapter implements JakartaJsonAdapter<Boolean>
        {
            @Override
            public Class<Boolean> type() { return Boolean.class; }

            @Override
            public JsonValue serialize(JakartaJsonFactories jsonFactories, Boolean object,
                                       Function<Object, JsonValue> serialize )
            {
                return type().cast( object ) ? JsonValue.TRUE : JsonValue.FALSE;
            }

            @Override
            public Boolean deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserialize )
            {
                switch( json.getValueType() )
                {
                    case TRUE:
                        return true;
                    case FALSE:
                        return false;
                    case NULL:
                        return null;
                    case NUMBER:
                        return ( (JsonNumber) json ).doubleValue() > 0;
                    case STRING:
                        return Boolean.valueOf( ( (JsonString) json ).getString() );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Boolean from " + json );
                }
            }
        }

        private static class IntegerAdapter implements JakartaJsonAdapter<Integer>
        {
            @Override
            public Class<Integer> type() { return Integer.class; }

            @Override
            public JsonValue serialize( JakartaJsonFactories jsonFactories,
                                        Integer object, Function<Object, JsonValue> serialize )
            {
                return jsonFactories.builderFactory().createObjectBuilder()
                                    .add( "value", type().cast( object ) )
                                    .build()
                                    .getJsonNumber( "value" );
            }

            @Override
            public Integer deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserialize )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return ( (JsonNumber) json ).intValueExact();
                    case STRING:
                        String string = ( (JsonString) json ).getString();
                        return string.isEmpty() ? 0 : Integer.parseInt( string );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Integer from " + json );
                }
            }
        }

        private static class LongAdapter implements JakartaJsonAdapter<Long>
        {
            @Override
            public Class<Long> type() { return Long.class; }

            @Override
            public JsonValue serialize( JakartaJsonFactories jsonFactories,
                                        Long object, Function<Object, JsonValue> serialize )
            {
                return jsonFactories.builderFactory().createObjectBuilder()
                                    .add( "value", type().cast( object ) )
                                    .build()
                                    .getJsonNumber( "value" );
            }

            @Override
            public Long deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserialize )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return ( (JsonNumber) json ).longValueExact();
                    case STRING:
                        String string = ( (JsonString) json ).getString();
                        return string.isEmpty() ? 0L : Long.parseLong( string );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Long from " + json );
                }
            }
        }

        private static class ShortAdapter implements JakartaJsonAdapter<Short>
        {
            @Override
            public Class<Short> type() { return Short.class; }

            @Override
            public JsonValue serialize( JakartaJsonFactories jsonFactories,
                                        Short object, Function<Object, JsonValue> serialize )
            {
                return jsonFactories.builderFactory().createObjectBuilder()
                                    .add( "value", type().cast( object ) )
                                    .build()
                                    .getJsonNumber( "value" );
            }

            @Override
            public Short deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserialize )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return (short) ( (JsonNumber) json ).intValueExact();
                    case STRING:
                        String string = ( (JsonString) json ).getString();
                        return string.isEmpty() ? 0 : Short.parseShort( string );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Short from " + json );
                }
            }
        }

        private static class ByteAdapter implements JakartaJsonAdapter<Byte>
        {
            @Override
            public Class<Byte> type() { return Byte.class; }

            @Override
            public JsonValue serialize( JakartaJsonFactories jsonFactories,
                                        Byte object, Function<Object, JsonValue> serialize )
            {
                return jsonFactories.builderFactory().createObjectBuilder()
                                    .add( "value", type().cast( object ) )
                                    .build()
                                    .getJsonNumber( "value" );
            }

            @Override
            public Byte deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserialize )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return (byte) ( (JsonNumber) json ).intValueExact();
                    case STRING:
                        String string = ( (JsonString) json ).getString();
                        return string.isEmpty() ? 0 : Byte.parseByte( string );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Byte from " + json );
                }
            }
        }

        private static class FloatAdapter implements JakartaJsonAdapter<Float>
        {
            @Override
            public Class<Float> type() { return Float.class; }

            @Override
            public JsonValue serialize( JakartaJsonFactories jsonFactories,
                                        Float object, Function<Object, JsonValue> serialize )
            {
                return jsonFactories.builderFactory().createObjectBuilder()
                                    .add( "value", type().cast( object ) )
                                    .build()
                                    .getJsonNumber( "value" );
            }

            @Override
            public Float deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserialize )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return (float) ( (JsonNumber) json ).doubleValue();
                    case STRING:
                        String string = ( (JsonString) json ).getString();
                        return string.isEmpty() ? 0F : Float.parseFloat( string );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Float from " + json );
                }
            }
        }

        private static class DoubleAdapter implements JakartaJsonAdapter<Double>
        {
            @Override
            public Class<Double> type() { return Double.class; }

            @Override
            public JsonValue serialize( JakartaJsonFactories jsonFactories,
                                        Double object, Function<Object, JsonValue> serialize )
            {
                return jsonFactories.builderFactory().createObjectBuilder()
                                    .add( "value", type().cast( object ) )
                                    .build()
                                    .getJsonNumber( "value" );
            }

            @Override
            public Double deserialize( JsonValue json, BiFunction<JsonValue, ValueType, Object> deserialize )
            {
                switch( json.getValueType() )
                {
                    case NULL:
                        return null;
                    case NUMBER:
                        return ( (JsonNumber) json ).doubleValue();
                    case STRING:
                        String string = ( (JsonString) json ).getString();
                        return string.isEmpty() ? 0D : Double.parseDouble( string );
                    default:
                        throw new SerializationException( "Don't know how to deserialize Double from " + json );
                }
            }
        }
    }
}
