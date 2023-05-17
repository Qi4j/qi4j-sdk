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
package org.qi4j.serialization.javaxjson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
import org.qi4j.api.association.AssociationDescriptor;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.composite.StatefulAssociationCompositeDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.serialization.Converter;
import org.qi4j.api.serialization.Converters;
import org.qi4j.api.serialization.SerializationException;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.type.ArrayType;
import org.qi4j.api.type.CollectionType;
import org.qi4j.api.type.MapType;
import org.qi4j.api.type.StatefulAssociationValueType;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.spi.serialization.AbstractTextDeserializer;
import org.qi4j.spi.serialization.JsonDeserializer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static org.qi4j.api.util.Collectors.toMapWithNullValues;
import static org.qi4j.serialization.javaxjson.JavaxJson.asString;
import static org.qi4j.serialization.javaxjson.JavaxJson.requireJsonArray;
import static org.qi4j.serialization.javaxjson.JavaxJson.requireJsonObject;
import static org.qi4j.serialization.javaxjson.JavaxJson.requireJsonStructure;

public class JavaxJsonDeserializer extends AbstractTextDeserializer
    implements JsonDeserializer, Initializable
{
    @This
    private JavaxJsonFactories jsonFactories;

    @This
    private Converters converters;

    @This
    private JavaxJsonAdapters adapters;

    @Uses
    private ServiceDescriptor descriptor;

    private JavaxJsonSettings settings;
    private JsonString emptyJsonString;

    @Override
    public void initialize() throws Exception
    {
        settings = JavaxJsonSettings.orDefault( descriptor.metaInfo( JavaxJsonSettings.class ) );
        emptyJsonString = jsonFactories.builderFactory().createObjectBuilder().add( "s", "" ).build()
                                       .getJsonString( "s" );
    }

    @Override
    public <T> T deserialize( ModuleDescriptor module, ValueType valueType, Reader state )
    {
        // JSR-353 Does not allow reading "out of structure" values
        // See https://www.jcp.org/en/jsr/detail?id=353
        // And commented JsonReader#readValue() method in the javax.json API
        // BUT, it will be part of the JsonReader contract in the next version
        // See https://www.jcp.org/en/jsr/detail?id=374
        // Implementation by provider is optional though, so we'll always need a default implementation here.
        // Fortunately, JsonParser has new methods allowing to read structures while parsing so it will be easy to do.
        // In the meantime, a poor man's implementation reading the json into memory will do.
        // TODO Revisit values out of structure JSON deserialization when JSR-374 is out
        String stateString;
        try( BufferedReader buffer = new BufferedReader( state ) )
        {
            stateString = buffer.lines().collect( joining( "\n" ) );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
        // We want plain Strings, BigDecimals, BigIntegers to be deserialized even when unquoted
        Function<String, T> plainValueFunction = string ->
        {
            String poorMans = "{\"value\":" + string + "}";
            JsonObject poorMansJson = jsonFactories.readerFactory()
                                                   .createReader( new StringReader( poorMans ) )
                                                   .readObject();
            JsonValue value = poorMansJson.get( "value" );
            return fromJson( module, valueType, value );
        };
        Function<String, T> outOfStructureFunction = string ->
        {
            // Is this an unquoted plain value?
            try
            {
                return plainValueFunction.apply( '"' + string + '"' );
            }
            catch( JsonParsingException ex )
            {
                return plainValueFunction.apply( string );
            }
        };
        try( JsonParser parser = jsonFactories.parserFactory().createParser( new StringReader( stateString ) ) )
        {
            if( parser.hasNext() )
            {
                JsonParser.Event e = parser.next();
                switch( e )
                {
                    case VALUE_NULL:
                        return null;
                    case START_ARRAY:
                    case START_OBJECT:
                        // JSON Structure
                        try( JsonReader reader = jsonFactories.readerFactory()
                                                              .createReader( new StringReader( stateString ) ) )
                        {
                            return fromJson( module, valueType, reader.read() );
                        }
                    default:
                        // JSON Value out of structure
                        return outOfStructureFunction.apply( stateString );
                }
            }
        }
        catch( JsonParsingException ex )
        {
            return outOfStructureFunction.apply( stateString );
        }
        // Empty state string?
        return fromJson( module, valueType, emptyJsonString );
    }

    @Override
    public <T> T fromJson( ModuleDescriptor module, ValueType valueType, JsonValue state )
    {
        return doDeserialize( module, valueType, state );
    }

    @SuppressWarnings( "unchecked" )
    private <T> T doDeserialize( ModuleDescriptor module, ValueType valueType, JsonValue json )
    {
        if( json == null || JsonValue.NULL.equals( json ) )
        {
            return null;
        }
        Converter<Object> converter = converters.converterFor( valueType );
        if( converter != null )
        {
            return (T) converter.fromString( doDeserialize( module, ValueType.STRING, json ).toString() );
        }
        JavaxJsonAdapter<?> adapter = adapters.adapterFor( valueType );
        if( adapter != null )
        {
            return (T) adapter.deserialize( json, ( jsonValue, type ) -> doDeserialize( module, type, jsonValue ) );
        }
        Class<? extends ValueType> valueTypeClass = valueType.getClass();
        if( ArrayType.class.isAssignableFrom( valueTypeClass ) )
        {
            return (T) deserializeArray( module, (ArrayType) valueType, json );
        }
        if( CollectionType.class.isAssignableFrom( valueTypeClass ) )
        {
            return (T) deserializeCollection( module, (CollectionType) valueType, requireJsonArray( json ) );
        }
        if( MapType.class.isAssignableFrom( valueTypeClass ) )
        {
            return (T) deserializeMap( module, (MapType) valueType, requireJsonStructure( json ) );
        }
        if( StatefulAssociationValueType.class.isAssignableFrom( valueTypeClass ) )
        {
            return (T) deserializeStatefulAssociationValue( module, (StatefulAssociationValueType<?>) valueType,
                                                            requireJsonObject( json ) );
        }
        return doGuessDeserialize( module, valueType, json );
    }

    private Object deserializeArray( ModuleDescriptor module, ArrayType arrayType, JsonValue json )
    {
        if( arrayType.isArrayOfPrimitiveBytes() && json.getValueType() == JsonValue.ValueType.STRING )
        {
            byte[] bytes = asString( json ).getBytes( UTF_8 );
            return Base64.getDecoder().decode( bytes );
        }
        if( json.getValueType() == JsonValue.ValueType.ARRAY )
        {
            CollectionType collectionType = CollectionType.listOf( arrayType.collectedType() );
            List<Object> collection = (List<Object>) deserializeCollection( module,
                                                                            collectionType,
                                                                            requireJsonArray( json ) );
            Object array = Array.newInstance( arrayType.collectedType().primaryType(), collection.size() );
            for( int idx = 0; idx < collection.size(); idx++ )
            {
                Array.set( array, idx, collection.get( idx ) );
            }
            return array;
        }
        throw new SerializationException( "Don't know how to deserialize " + arrayType + " from " + json );
    }

    @SuppressWarnings( "unchecked" )
    private <T> T doGuessDeserialize( ModuleDescriptor module, ValueType valueType, JsonValue json )
    {
        switch( json.getValueType() )
        {
            case OBJECT:
                JsonObject object = (JsonObject) json;
                String typeInfo = object.getString( settings.getTypeInfoPropertyName(),
                                                    valueType.primaryType().getName() );
                StatefulAssociationCompositeDescriptor descriptor = statefulCompositeDescriptorFor( module, typeInfo );
                if( descriptor != null )
                {
                    return (T) deserializeStatefulAssociationValue( ( (CompositeDescriptor) descriptor ).module(),
                                                                    descriptor.valueType(),
                                                                    object );
                }
            default:
                throw new SerializationException( "Don't know how to deserialize " + valueType + " from " + json );
        }
    }

    private <T> Collection<T> deserializeCollection( ModuleDescriptor module, CollectionType collectionType,
                                                     JsonArray json )
    {
        return (Collection<T>) json.stream()
                                   .map( item -> doDeserialize( module, collectionType.collectedType(), item ) )
                                   .collect( toCollection(
                                       () -> collectionType.isSet() ? new LinkedHashSet<>() : new ArrayList<>() ) );
    }

    /**
     * Map deserialization.
     *
     * {@literal JsonObject}s are deserialized to {@literal Map<String, ?>}.
     * {@literal JsonArray}s of key/value {@literal JsonObject}s are deserialized to {@literal Map<?, ?>}.
     */
    private Map<?, ?> deserializeMap( ModuleDescriptor module, MapType mapType, JsonStructure json )
    {
        if( json.getValueType() == JsonValue.ValueType.OBJECT )
        {
            JsonObject object = (JsonObject) json;
            return object.entrySet().stream()
                         .map( entry -> new AbstractMap.SimpleImmutableEntry<>(
                             entry.getKey(),
                             doDeserialize( module, mapType.valueType(), entry.getValue() ) ) )
                         .collect( toMapWithNullValues( LinkedHashMap::new ) );
        }
        if( json.getValueType() == JsonValue.ValueType.ARRAY )
        {
            JsonArray array = (JsonArray) json;
            return array.stream()
                        .map( JsonObject.class::cast )
                        .map( entry -> new AbstractMap.SimpleImmutableEntry<>(
                            doDeserialize( module, mapType.keyType(), entry.get( "key" ) ),
                            doDeserialize( module, mapType.valueType(), entry.get( "value" ) )
                        ) )
                        .collect( toMapWithNullValues( LinkedHashMap::new ) );
        }
        throw new SerializationException( "Don't know how to deserialize " + mapType + " from " + json );
    }

    private Object deserializeStatefulAssociationValue( ModuleDescriptor module,
                                                        StatefulAssociationValueType<?> valueType,
                                                        JsonObject json )
    {
        String typeInfoName = settings.getTypeInfoPropertyName();
        String typeInfo = json.getString( typeInfoName, null );
        if( typeInfo != null )
        {
            StatefulAssociationCompositeDescriptor descriptor = statefulCompositeDescriptorFor( module, typeInfo );
            if( descriptor == null )
            {
                throw new SerializationException(
                    typeInfoName + ": " + typeInfo + " could not be resolved while deserializing " + json );
            }
            valueType = descriptor.valueType();
        }
        ValueBuilder builder = module.instance().newValueBuilderWithState(
            valueType.primaryType(),
            propertyFunction( valueType.module(), json ),
            associationFunction( valueType.module(), json ),
            manyAssociationFunction( valueType.module(), json ),
            namedAssociationFunction( valueType.module(), json ) );
        return builder.newInstance();
    }

    private Function<PropertyDescriptor, Object> propertyFunction( ModuleDescriptor module, JsonObject object )
    {
        return property ->
        {
            JsonValue jsonValue = object.get( property.qualifiedName().name() );
            if( jsonValue != null )
            {
                Object value;
                Converter converter = converters.converterFor( property );
                if( converter != null )
                {
                    value = converter.fromString( doDeserialize( module, ValueType.STRING, jsonValue ) );
                }
                else
                {
                    value = doDeserialize( module, property.valueType(), jsonValue );
                }
                if( property.isImmutable() )
                {
                    if( value instanceof Set )
                    {
                        return unmodifiableSet( (Set<?>) value );
                    }
                    else if( value instanceof List )
                    {
                        return unmodifiableList( (List<?>) value );
                    }
                    else if( value instanceof Map )
                    {
                        return unmodifiableMap( (Map<?, ?>) value );
                    }
                }
                return value;
            }
            return property.resolveInitialValue( module );
        };
    }

    private Function<AssociationDescriptor, EntityReference> associationFunction( ModuleDescriptor module,
                                                                                  JsonObject object )
    {
        return association -> doDeserialize( module, ValueType.ENTITY_REFERENCE,
                                             object.get( association.qualifiedName().name() ) );
    }

    private Function<AssociationDescriptor, Stream<EntityReference>> manyAssociationFunction( ModuleDescriptor module,
                                                                                              JsonObject object )
    {
        return association ->
        {
            List<EntityReference> list = doDeserialize( module, ENTITY_REF_LIST_VALUE_TYPE,
                                                        object.get( association.qualifiedName().name() ) );
            return list == null ? Stream.empty() : list.stream();
        };
    }

    private Function<AssociationDescriptor, Stream<Map.Entry<String, EntityReference>>> namedAssociationFunction(
        ModuleDescriptor module, JsonObject object )
    {
        return association ->
        {
            Map<String, EntityReference> map = doDeserialize( module, ENTITY_REF_MAP_VALUE_TYPE,
                                                              object.get( association.qualifiedName().name() ) );
            return map == null ? Stream.empty() : map.entrySet().stream();
        };
    }
}
