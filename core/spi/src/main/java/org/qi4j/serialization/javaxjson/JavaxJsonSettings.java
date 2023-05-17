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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.qi4j.api.type.ValueType;
import org.qi4j.spi.serialization.SerializationSettings;

public class JavaxJsonSettings extends SerializationSettings<JavaxJsonSettings>
{
    public static final JavaxJsonSettings DEFAULT = new JavaxJsonSettings();

    public static JavaxJsonSettings orDefault( JavaxJsonSettings settings )
    {
        return settings != null ? settings : DEFAULT;
    }

    private String jsonProviderClassName;
    private Map<String, Object> jsonParserProperties;
    private Map<String, Object> jsonGeneratorProperties;
    private String typeInfoPropertyName;
    private Map<ValueType, JavaxJsonAdapter<?>> adapters;

    public JavaxJsonSettings()
    {
        jsonParserProperties = new HashMap<String, Object>()
        {{
            put( "org.apache.johnzon.supports-comments", true );
        }};
        jsonGeneratorProperties = new HashMap<>();
        typeInfoPropertyName = "_type";
        adapters = new LinkedHashMap<>();
    }

    public String getJsonProviderClassName()
    {
        return jsonProviderClassName;
    }

    public void setJsonProviderClassName( String jsonProviderClassName )
    {
        this.jsonProviderClassName = jsonProviderClassName;
    }

    public Map<String, Object> getJsonParserProperties()
    {
        return jsonParserProperties;
    }

    public void setJsonParserProperties( Map<String, Object> jsonParserProperties )
    {
        this.jsonParserProperties = jsonParserProperties;
    }

    public Map<String, Object> getJsonGeneratorProperties()
    {
        return jsonGeneratorProperties;
    }

    public void setJsonGeneratorProperties( Map<String, Object> jsonGeneratorProperties )
    {
        this.jsonGeneratorProperties = jsonGeneratorProperties;
    }

    public String getTypeInfoPropertyName()
    {
        return typeInfoPropertyName;
    }

    public void setTypeInfoPropertyName( String typeInfoPropertyName )
    {
        this.typeInfoPropertyName = typeInfoPropertyName;
    }

    public Map<ValueType, JavaxJsonAdapter<?>> getAdapters()
    {
        return adapters;
    }

    public JavaxJsonSettings withTypeInfoPropertyName( String typeInfoPropertyName )
    {
        this.typeInfoPropertyName = typeInfoPropertyName;
        return this;
    }

    public JavaxJsonSettings withJsonAdapter( ValueType valueType, JavaxJsonAdapter<?> adapter )
    {
        adapters.put( valueType, adapter );
        return this;
    }

    public JavaxJsonSettings withJsonAdapter( JavaxJsonAdapter<?> adapter )
    {
        return withJsonAdapter( ValueType.of( adapter.type() ), adapter );
    }
}
