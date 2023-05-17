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

package org.qi4j.library.rest.client.responsereader;

import java.io.IOException;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.type.ValueCompositeType;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.library.rest.client.spi.ResponseReader;
import org.qi4j.serialization.javaxjson.JavaxJsonFactories;
import org.qi4j.spi.serialization.JsonDeserializer;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.resource.ResourceException;

/**
 * JAVADOC
 */
public class JSONResponseReader
    implements ResponseReader
{
    @Structure
    private ModuleDescriptor module;

    @Service
    private JsonDeserializer jsonDeserializer;

    @Service
    private JavaxJsonFactories jsonFactories;

    @Override
    public Object readResponse( Response response, Class<?> resultType )
    {
        if( response.getEntity().getMediaType().equals( MediaType.APPLICATION_JSON ) )
        {
            if( ValueComposite.class.isAssignableFrom( resultType ) )
            {
                String jsonValue = response.getEntityAsText();
                ValueCompositeType valueType = module.valueDescriptor( resultType.getName() ).valueType();
                return jsonDeserializer.deserialize( module, valueType, jsonValue );
            }
            else if( resultType.equals( Form.class ) )
            {
                try( JsonReader reader = jsonFactories.readerFactory()
                                                      .createReader( response.getEntity().getReader() ) )
                {
                    JsonObject jsonObject = reader.readObject();
                    Form form = new Form();
                    jsonObject.forEach(
                        ( key, value ) ->
                        {
                            String valueString = value.getValueType() == JsonValue.ValueType.STRING
                                                 ? ( (JsonString) value ).getString()
                                                 : value.toString();
                            form.set( key, valueString );
                        } );
                    return form;
                }
                catch( IOException | JsonException e )
                {
                    throw new ResourceException( e );
                }
            }
        }
        return null;
    }
}
