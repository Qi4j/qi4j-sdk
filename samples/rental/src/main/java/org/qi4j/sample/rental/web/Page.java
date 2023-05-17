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

package org.qi4j.sample.rental.web;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.qi4j.api.Qi4jAPI;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.qi4j.api.util.AccessibleObjects.accessible;

@Concerns( PageUowManagement.class )
@Mixins( { Page.MountPointMixin.class, Page.DefaultPageRenderMixin.class } )
public interface Page
    extends ServiceComposite
{
    String XHTML = "http://www.w3.org/1999/xhtml";
    String QI = "http://qi4j.org/ns/2009/quikit";

    /**
     * Returns the URL where the Page is mounted.
     *
     * @return the URL mountpoint.
     */
    String mountPoint();

    void render( QuikitContext context )
        throws RenderException;

    abstract class DefaultPageRenderMixin
        implements Page
    {
        public void render( QuikitContext context )
            throws RenderException
        {
            Document dom = context.dom();
            Element htmlElement = dom.getDocumentElement();
            Element bodyElement = (Element) htmlElement.getElementsByTagNameNS( Page.XHTML, "body" ).item( 0 );
            parseElement( context, bodyElement, htmlElement );
        }

        private void parseElement( QuikitContext context, Element element, Element parent )
            throws RenderException
        {
            String method = element.getAttributeNS( Page.QI, "method" );
            if( method.length() > 0 )
            {
                context.setDynamic( method, element, parent );
                execute( context, element, parent );
                return;
            }
            NodeList nodes = element.getChildNodes();
            for( int i = 0; i < nodes.getLength(); i++ )
            {
                Node node = nodes.item( i );
                if( node instanceof Element )
                {
                    parseElement( context, (Element) node, element );
                }
            }
        }

        private void execute( QuikitContext context, Element element, Element parent )
            throws RenderException
        {
            Class<? extends Composite> compositeType =
                (Class<Composite>) Qi4jAPI.FUNCTION_DESCRIPTOR_FOR.apply( context.page() ).types().findFirst().orElse( null );
            try
            {
                Method method = findMethod( context.methodName(), compositeType );
                Object result = method.invoke( context.page(), context );
                if( result instanceof String )
                {
                    element.setTextContent( (String) result );
                    return;
                }
                if( result instanceof Node )
                {
                    parent.replaceChild( (Node) result, element );
                    return;
                }
                if( result instanceof List )
                {
                    for( Node node : (List<Node>) result )
                    {
                        element.appendChild( node );
                    }
                    return;
                }
                if( result.getClass().isArray() )
                {
                    Class type = result.getClass().getComponentType();
                    if( Node.class.isAssignableFrom( type ) )
                    {
                        for( Node node : (Node[]) result )
                        {
                            element.appendChild( node );
                        }
                    }
                    return;
                }
//              TODO: Future!!!
//                if( result instanceof EntityComposite )
//                {
                // Locate HTML template for reault.type()
                // If not present, use EntityComposite.html
                // repeat rendering.
                // Need mechanism to pass the "reference"
//                }
                element.setTextContent( result.toString() );
            }

            catch( NoSuchMethodException e )
            {
                String message = "Method '" + context.methodName() + "' does not exist in " + compositeType.getSimpleName();
                throw new RenderException( message, e );
            }
            catch( IllegalAccessException e )
            {
                String message = "Method '" + context.methodName() + "' is not public in " + compositeType.getSimpleName();
                throw new RenderException( message, e );
            }
            catch( InvocationTargetException e )
            {
                if( e.getTargetException() instanceof RenderException )
                {
                    throw ( (RenderException) e.getTargetException() );
                }
                throw new RenderException( "Method '" + context.methodName() + "' threw an exception.", e );
            }
        }

        private Method findMethod( String methodName, Class<? extends Composite> compositeType )
            throws NoSuchMethodException
        {
            // TODO: Add caching since locating the methods and the throwing of exceptions are expensive.
            return accessible( compositeType.getMethod( methodName, QuikitContext.class ) );
        }
    }

    abstract class MountPointMixin
        implements Page
    {
        @Uses
        ServiceDescriptor descriptor;

        public String mountPoint()
        {
            return descriptor.metaInfo( PageMetaInfo.class ).mountPoint();
        }
    }
}
