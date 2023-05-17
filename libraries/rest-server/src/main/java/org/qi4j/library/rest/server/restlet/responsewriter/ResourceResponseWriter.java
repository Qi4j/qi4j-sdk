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

package org.qi4j.library.rest.server.restlet.responsewriter;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.library.rest.common.Resource;
import org.qi4j.library.rest.common.link.LinksUtil;
import org.qi4j.spi.serialization.JsonSerializer;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;

/**
 * ResponseWriter for ResourceValues
 */
public class ResourceResponseWriter extends AbstractResponseWriter
{
    private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM );

    @Service
    private Configuration cfg;

    @Service
    private JsonSerializer jsonSerializer;

    @Override
    public boolean writeResponse( final Object result, final Response response )
        throws ResourceException
    {
        if( result instanceof Resource )
        {
            Resource resourceValue = (Resource) result;

            // Allowed methods
            response.getAllowedMethods().add( Method.GET );
            if( resourceValue.commands().get().stream().anyMatch( LinksUtil.withRel( "delete" ) ) )
            {
                response.getAllowedMethods().add( Method.DELETE );
            }
            if( resourceValue.commands().get().stream().anyMatch( LinksUtil.withRel( "update" ) ) )
            {
                response.getAllowedMethods().add( Method.PUT );
            }

            // Response according to what client accepts
            MediaType type = getVariant( response.getRequest(), ENGLISH, supportedMediaTypes ).getMediaType();
            if( MediaType.APPLICATION_JSON.equals( type ) )
            {
                String json = jsonSerializer.serialize( resourceValue );
                response.setEntity( new StringRepresentation( json, MediaType.APPLICATION_JSON ) );
                return true;
            }
            else if( MediaType.TEXT_HTML.equals( type ) )
            {
                Representation rep = new WriterRepresentation( MediaType.TEXT_HTML )
                {
                    @Override
                    public void write( Writer writer )
                        throws IOException
                    {
                        Map<String, Object> context = new HashMap<>();
                        context.put( "request", response.getRequest() );
                        context.put( "response", response );
                        context.put( "result", result );
                        try
                        {
                            cfg.getTemplate( "resource.htm" ).process( context, writer );
                        }
                        catch( TemplateException e )
                        {
                            throw new IOException( e );
                        }
                    }
                };
                response.setEntity( rep );
                return true;
            }
        }

        return false;
    }
}
