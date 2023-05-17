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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import org.qi4j.api.type.ValueType;
import org.qi4j.spi.serialization.SerializationSettings;

/**
 * javax.xml settings.
 *
 * Must be registered as meta-info at assembly time.
 */
public class JavaxXmlSettings extends SerializationSettings<JavaxXmlSettings>
{
    public static final JavaxXmlSettings DEFAULT = new JavaxXmlSettings();

    public static JavaxXmlSettings orDefault( JavaxXmlSettings settings )
    {
        return settings != null ? settings : DEFAULT;
    }

    private String documentBuilderFactoryClassName;
    private Map<String, Boolean> documentBuilderFactoryFeatures;
    private Map<String, Object> documentBuilderFactoryAttributes;

    private String transformerFactoryClassName;
    private Map<String, Boolean> transformerFactoryFeatures;
    private Map<String, Object> transformerFactoryAttributes;

    private String rootTagName;
    private String collectionTagName;
    private String collectionElementTagName;
    private String mapTagName;
    private String mapEntryTagName;
    private String valueTagName;
    private String typeInfoTagName;

    private Map<ValueType, JavaxXmlAdapter<?>> adapters;

    public JavaxXmlSettings()
    {
        documentBuilderFactoryFeatures = new HashMap<String, Boolean>()
        {{
            put( XMLConstants.FEATURE_SECURE_PROCESSING, true );
        }};
        documentBuilderFactoryAttributes = new HashMap<>();

        transformerFactoryFeatures = new HashMap<String, Boolean>()
        {{
            put( XMLConstants.FEATURE_SECURE_PROCESSING, true );
        }};
        transformerFactoryAttributes = new HashMap<String, Object>()
        {{
            put( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
            put( XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "" );
        }};

        rootTagName = "state";
        collectionTagName = "collection";
        collectionElementTagName = "element";
        mapTagName = "map";
        mapEntryTagName = "entry";
        valueTagName = "value";
        typeInfoTagName = "_type";

        adapters = new LinkedHashMap<>();
    }

    public String getDocumentBuilderFactoryClassName()
    {
        return documentBuilderFactoryClassName;
    }

    public void setDocumentBuilderFactoryClassName( String documentBuilderFactoryClassName )
    {
        this.documentBuilderFactoryClassName = documentBuilderFactoryClassName;
    }

    public Map<String, Boolean> getDocumentBuilderFactoryFeatures()
    {
        return documentBuilderFactoryFeatures;
    }

    public void setDocumentBuilderFactoryFeatures( Map<String, Boolean> documentBuilderFactoryFeatures )
    {
        this.documentBuilderFactoryFeatures = documentBuilderFactoryFeatures;
    }

    public Map<String, Object> getDocumentBuilderFactoryAttributes()
    {
        return documentBuilderFactoryAttributes;
    }

    public void setDocumentBuilderFactoryAttributes( Map<String, Object> documentBuilderFactoryAttributes )
    {
        this.documentBuilderFactoryAttributes = documentBuilderFactoryAttributes;
    }

    public String getTransformerFactoryClassName()
    {
        return transformerFactoryClassName;
    }

    public void setTransformerFactoryClassName( String transformerFactoryClassName )
    {
        this.transformerFactoryClassName = transformerFactoryClassName;
    }

    public Map<String, Boolean> getTransformerFactoryFeatures()
    {
        return transformerFactoryFeatures;
    }

    public void setTransformerFactoryFeatures( Map<String, Boolean> transformerFactoryFeatures )
    {
        this.transformerFactoryFeatures = transformerFactoryFeatures;
    }

    public Map<String, Object> getTransformerFactoryAttributes()
    {
        return transformerFactoryAttributes;
    }

    public void setTransformerFactoryAttributes( Map<String, Object> transformerFactoryAttributes )
    {
        this.transformerFactoryAttributes = transformerFactoryAttributes;
    }

    public String getRootTagName()
    {
        return rootTagName;
    }

    public void setRootTagName( final String rootTagName )
    {
        this.rootTagName = rootTagName;
    }

    public String getCollectionTagName()
    {
        return collectionTagName;
    }

    public void setCollectionTagName( final String collectionTagName )
    {
        this.collectionTagName = collectionTagName;
    }

    public String getCollectionElementTagName()
    {
        return collectionElementTagName;
    }

    public void setCollectionElementTagName( final String collectionElementTagName )
    {
        this.collectionElementTagName = collectionElementTagName;
    }

    public String getMapTagName()
    {
        return mapTagName;
    }

    public void setMapTagName( final String mapTagName )
    {
        this.mapTagName = mapTagName;
    }

    public String getMapEntryTagName()
    {
        return mapEntryTagName;
    }

    public void setMapEntryTagName( final String mapEntryTagName )
    {
        this.mapEntryTagName = mapEntryTagName;
    }

    public String getValueTagName()
    {
        return valueTagName;
    }

    public void setValueTagName( final String valueTagName )
    {
        this.valueTagName = valueTagName;
    }

    public String getTypeInfoTagName()
    {
        return typeInfoTagName;
    }

    public void setTypeInfoTagName( final String typeInfoTagName )
    {
        this.typeInfoTagName = typeInfoTagName;
    }

    public Map<ValueType, JavaxXmlAdapter<?>> getAdapters()
    {
        return adapters;
    }
}
