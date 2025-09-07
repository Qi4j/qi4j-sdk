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
package org.qi4j.serialization.javaxxml;

import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.composite.StatefulAssociationCompositeDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Initializable;
import org.qi4j.api.serialization.Converter;
import org.qi4j.api.serialization.Converters;
import org.qi4j.api.serialization.Serialization.Options;
import org.qi4j.api.serialization.SerializationException;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.type.ArrayType;
import org.qi4j.api.type.EnumType;
import org.qi4j.api.type.MapType;
import org.qi4j.api.type.StatefulAssociationValueType;
import org.qi4j.spi.serialization.AbstractTextSerializer;
import org.qi4j.spi.serialization.XmlSerializer;
import org.qi4j.spi.util.ArrayIterable;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Base64;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.qi4j.api.util.Collectors.toMap;

/**
 * XML Serializer.
 */
public class JavaxXmlSerializer extends AbstractTextSerializer
    implements XmlSerializer, Initializable
{
    private static final String NULL_ELEMENT_NAME = "null";

    @This
    private JavaxXmlFactories xmlFactories;

    @This
    private Converters converters;

    @This
    private JavaxXmlAdapters adapters;

    @Uses
    private ServiceDescriptor descriptor;

    private JavaxXmlSettings settings;

    @Override
    public void initialize()
        throws Exception
    {
        settings = JavaxXmlSettings.orDefault(descriptor.metaInfo(JavaxXmlSettings.class));
    }

    @Override
    public void serialize(ModuleDescriptor module, Options options, Writer writer, @Optional Object object)
    {
        Document xmlDocument = toXml(module, options, object);
        if(xmlDocument == null)
        {
            return;
        }
        try
        {
            // We want plain text nodes to be serialized without surrounding elements
            if(xmlDocument.getNodeType() == Node.TEXT_NODE)
            {
                writer.write(xmlDocument.getNodeValue());
            }
            else
            {
                xmlFactories.serializationTransformer().transform(new DOMSource(xmlDocument),
                    new StreamResult(writer));
            }
        }
        catch(IOException ex)
        {
            throw new UncheckedIOException(ex);
        }
        catch(TransformerException ex)
        {
            throw new SerializationException("Unable to transform XML Document to String", ex);
        }
    }

    @Override
    public <T> Function<T, Document> toXmlFunction(ModuleDescriptor module, Options options)
    {
        return object -> doSerializeRoot(module, options, object);
    }

    private <T> Document doSerializeRoot(ModuleDescriptor module, Options options, T object)
    {
        Document doc = xmlFactories.newDocumentForSerialization();
        Element stateElement = doc.createElement(settings.getRootTagName());
        Node node = doSerialize(module, options, doc, object, true);
        stateElement.appendChild(node);
        doc.appendChild(stateElement);
        return doc;
    }

    private <T> Node doSerialize(ModuleDescriptor module, Options options, Document document, T object, boolean root)
    {
        if(object == null)
        {
            return document.createElement(NULL_ELEMENT_NAME);
        }
        Class<?> objectClass = object.getClass();
        Converter<Object> converter = converters.converterFor(objectClass);
        if(converter != null)
        {
            return doSerialize(module, options, document, converter.toString(object), false);
        }
        JavaxXmlAdapter<?> adapter = adapters.adapterFor(objectClass);
        if(adapter != null)
        {
            return adapter.serialize(module, options, document, object, value -> doSerialize(module, options, document, value, false));
        }
        if(EnumType.isEnum(objectClass))
        {
            return document.createTextNode(object.toString());
        }
        if(StatefulAssociationValueType.isStatefulAssociationValue(objectClass))
        {
            return serializeStatefulAssociationValue(module, options, document, object, root);
        }
        if(MapType.isMap(objectClass))
        {
            return serializeMap(module, options, document, (Map<?, ?>) object);
        }
        if(ArrayType.isArray(objectClass))
        {
            return serializeArray(module, options, document, object);
        }
        if(Iterable.class.isAssignableFrom(objectClass))
        {
            return serializeIterable(module, options, document, (Iterable<?>) object);
        }
        if(Stream.class.isAssignableFrom(objectClass))
        {
            return serializeStream(module, options, document, (Stream<?>) object);
        }
        throw new SerializationException("Don't know how to serialize " + object);
    }

    private <T> Node serializeStatefulAssociationValue(ModuleDescriptor module, Options options, Document document, T composite, boolean root)
    {
        CompositeInstance instance = Qi4jAPI.FUNCTION_COMPOSITE_INSTANCE_OF.apply((Composite) composite);
        StatefulAssociationCompositeDescriptor descriptor =
            (StatefulAssociationCompositeDescriptor) instance.descriptor();
        AssociationStateHolder state = (AssociationStateHolder) instance.state();
        StatefulAssociationValueType<?> valueType = descriptor.valueType();

        Element valueElement = document.createElement(settings.getValueTagName());
        valueType.properties().forEach(
            property ->
            {
                Object value = state.propertyFor(property.accessor()).get();
                Converter<Object> converter = converters.converterFor(property);
                if(converter != null)
                {
                    value = converter.toString(value);
                }
                Element element = document.createElement(property.qualifiedName().name());
                element.appendChild(doSerialize(module, options, document, value, false));
                valueElement.appendChild(element);
            });
        valueType.associations().forEach(
            association ->
            {
                EntityReference value = state.associationFor(association.accessor()).reference();
                Element element = document.createElement(association.qualifiedName().name());
                element.appendChild(doSerialize(module, options, document, value, false));
                valueElement.appendChild(element);
            }
        );
        valueType.manyAssociations().forEach(
            association ->
            {
                Stream<EntityReference> value = state.manyAssociationFor(association.accessor()).references();
                Element element = document.createElement(association.qualifiedName().name());
                element.appendChild(doSerialize(module, options, document, value, false));
                valueElement.appendChild(element);
            }
        );
        valueType.namedAssociations().forEach(
            association ->
            {
                Map<String, EntityReference> value = state.namedAssociationFor(association.accessor()).references()
                    .collect(toMap());
                Element element = document.createElement(association.qualifiedName().name());
                element.appendChild(doSerialize(module, options, document, value, false));
                valueElement.appendChild(element);
            }
        );
        if((root && options.rootTypeInfo()) || (!root && options.nestedTypeInfo()))
        {
            valueElement.setAttribute(settings.getTypeInfoTagName(), valueType.primaryType().getName());
        }
        return valueElement;
    }

    private Node serializeMap(ModuleDescriptor module, Options options, Document document, Map<?, ?> map)
    {
        Element mapElement = document.createElement(settings.getMapTagName());
        if(map.isEmpty())
        {
            return mapElement;
        }
        Function<Map.Entry, Node> complexMapping = entry ->
        {
            Element entryElement = document.createElement(settings.getMapEntryTagName());

            Element keyElement = document.createElement("key");
            keyElement.appendChild(doSerialize(module, options, document, entry.getKey(), false));
            entryElement.appendChild(keyElement);

            Element valueElement = document.createElement("value");
            valueElement.appendChild(doSerialize(module, options, document, entry.getValue(), false));
            entryElement.appendChild(valueElement);

            return entryElement;
        };

        if(map.keySet().iterator().next() instanceof CharSequence)
        {
            map.entrySet().stream()
                .map(entry ->
                {
                    try
                    {
                        Element element = document.createElement(entry.getKey().toString());
                        element.appendChild(doSerialize(module, options, document, entry.getValue(), false));
                        return element;
                    }
                    catch(DOMException ex)
                    {
                        // The key name cannot be encoded as a tag name, fallback to complex mapping
                        // Tag names cannot start with a digit, some characters cannot be escaped etc...
                        return complexMapping.apply(entry);
                    }
                })
                .forEach(mapElement::appendChild);
        }
        else
        {
            map.entrySet().stream()
                .map(complexMapping)
                .forEach(mapElement::appendChild);
        }
        return mapElement;
    }

    private <T> Node serializeArray(ModuleDescriptor module, Options options, Document document, T object)
    {
        ArrayType valueType = ArrayType.of(object.getClass());
        if(valueType.isArrayOfPrimitiveBytes())
        {
            byte[] base64 = Base64.getEncoder().encode((byte[]) object);
            return document.createCDATASection(new String(base64, UTF_8));
        }
        if(valueType.isArrayOfPrimitives())
        {
            return serializeIterable(module, options, document, new ArrayIterable(object));
        }
        return serializeStream(module, options, document, Stream.of((Object[]) object));
    }

    private Node serializeIterable(ModuleDescriptor module, Options options, Document document, Iterable<?> object)
    {
        return serializeStream(module, options, document, StreamSupport.stream(object.spliterator(), false));
    }

    private Node serializeStream(ModuleDescriptor module, Options options, Document document, Stream<?> object)
    {
        Element collectionElement = document.createElement(settings.getCollectionTagName());
        object.map(each -> doSerialize(module, options, document, each, false))
            .forEach(itemValueNode ->
            {
                Element itemElement = document.createElement(settings.getCollectionElementTagName());
                itemElement.appendChild(itemValueNode);
                collectionElement.appendChild(itemElement);
            });
        return collectionElement;
    }
}
