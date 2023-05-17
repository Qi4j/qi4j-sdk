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

package org.qi4j.library.rest.client.requestwriter;

import java.io.IOException;
import java.io.Writer;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.service.qualifier.Tagged;
import org.qi4j.api.serialization.Serialization;
import org.qi4j.api.serialization.SerializationException;
import org.qi4j.api.serialization.Serializer;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.api.value.ValueDescriptor;
import org.qi4j.library.rest.client.spi.RequestWriter;
import org.qi4j.spi.Qi4jSPI;
import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;

/**
 * Request writer for ValueComposites. Transfers value state to request reference as query parameters or JSON entity
 */
public class ValueCompositeRequestWriter
   implements RequestWriter
{
   @Structure
   private Qi4jSPI spi;

   @Service
   @Tagged( Serialization.Format.JSON )
   private Serializer serializer;

    @Override
   public boolean writeRequest(Object requestObject, Request request) throws ResourceException
   {
      if (requestObject instanceof ValueComposite)
      {
         // Value as parameter
         final ValueComposite valueObject = (ValueComposite) requestObject;
         if (request.getMethod().equals(Method.GET))
         {
            StateHolder holder = spi.stateOf( valueObject );
            final ValueDescriptor descriptor = spi.valueDescriptorFor( valueObject );

             final Reference ref = request.getResourceRef();
             ref.setQuery( null );
             descriptor.state().properties().forEach( propertyDescriptor -> {
                 try
                 {
                     Object value = holder.propertyFor( propertyDescriptor.accessor() ).get();
                     String param;
                     if( value == null )
                     {
                         param = null;
                     }
                     else
                     {
                         param = serializer.serialize( value );
                     }
                     ref.addQueryParameter( propertyDescriptor.qualifiedName().name(), param );
                 }
                 catch( SerializationException e )
                 {
                     throw new ResourceException( e );
                 }
             } );
         }
         else
         {
            request.setEntity(new WriterRepresentation( MediaType.APPLICATION_JSON )
            {
                @Override
                public void write( Writer writer )
                    throws IOException
                {
                   setCharacterSet( CharacterSet.UTF_8 );
                   serializer.serialize( writer, valueObject );
                }
            });
         }

         return true;
      }

      return false;
   }
}
