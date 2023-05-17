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
package org.qi4j.bootstrap.builder;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.activation.ActivationEventListenerRegistration;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.ApplicationPassivationThread;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;

/**
 * Application Builder.
 */
public class ApplicationBuilder
    implements ActivationEventListenerRegistration
{
    private final String applicationName;
    private String applicationVersion;
    private Application.Mode applicationMode;
    private final List<Object> metaInfos = new ArrayList<>();
    private boolean passivationShutdownHook;
    private final Map<String, LayerDeclaration> layers = new HashMap<>();
    private final List<ActivationEventListener> activationListeners = new ArrayList<>();

    public ApplicationBuilder( String applicationName )
    {
        this.applicationName = applicationName;
    }

    public ApplicationBuilder version( String version )
    {
        applicationVersion = version;
        return this;
    }

    public ApplicationBuilder mode( Application.Mode mode )
    {
        applicationMode = mode;
        return this;
    }

    public ApplicationBuilder metaInfo( Object... metaInfos )
    {
        Collections.addAll( this.metaInfos, metaInfos );
        return this;
    }

    /**
     * Register a JVM shutdown hook that passivate the Application.
     *
     * The hook is registered after activating the Application and before {@link #afterActivation()}.
     *
     * @return This builder
     */
    public ApplicationBuilder withPassivationShutdownHook()
    {
        this.passivationShutdownHook = true;
        return this;
    }

    /**
     * Create and activate a new Application.
     * @return Activated Application
     * @throws AssemblyException if the assembly failed
     * @throws ActivationException if the activation failed
     */
    public Application newApplication()
        throws AssemblyException, ActivationException
    {
        Energy4Java qi4j = new Energy4Java();
        ApplicationDescriptor model = qi4j.newApplicationModel( factory -> {
            ApplicationAssembly assembly = factory.newApplicationAssembly();
            assembly.setName( applicationName );
            if( applicationVersion != null )
            {
                assembly.setVersion( applicationVersion );
            }
            if( applicationMode != null )
            {
                assembly.setMode( applicationMode );
            }
            for( Object metaInfo : metaInfos )
            {
                assembly.setMetaInfo( metaInfo );
            }
            HashMap<String, LayerAssembly> createdLayers = new HashMap<>();
            for( Map.Entry<String, LayerDeclaration> entry : layers.entrySet() )
            {
                LayerAssembly layer = entry.getValue().createLayer( assembly );
                createdLayers.put( entry.getKey(), layer );
            }
            for( LayerDeclaration layer : layers.values() )
            {
                layer.initialize( createdLayers );
            }
            return assembly;
        } );
        Application application = model.newInstance( qi4j.api() );
        for( ActivationEventListener activationListener : activationListeners )
        {
            application.registerActivationEventListener( activationListener );
        }
        beforeActivation();
        application.activate();
        if( passivationShutdownHook )
        {
            Runtime.getRuntime().addShutdownHook( new ApplicationPassivationThread( application ) );
        }
        afterActivation();
        return application;
    }

    /**
     * Called before application activation.
     */
    protected void beforeActivation()
    {
    }

    /**
     * Called after application activation.
     */
    protected void afterActivation()
    {
    }

    @Override
    public void registerActivationEventListener( ActivationEventListener listener )
    {
        activationListeners.add( listener );
    }

    @Override
    public void deregisterActivationEventListener( ActivationEventListener listener )
    {
        activationListeners.remove( listener );
    }

    /**
     * Declare Layer.
     * @param layerName Name of the Layer
     * @return Layer declaration for the given name, new if did not already exists
     */
    public LayerDeclaration withLayer( String layerName )
    {
        LayerDeclaration layerDeclaration = layers.get( layerName );
        if( layerDeclaration != null )
        {
            return layerDeclaration;
        }
        layerDeclaration = new LayerDeclaration( layerName );
        layers.put( layerName, layerDeclaration );
        return layerDeclaration;
    }

    /**
     * Load an ApplicationBuilder from a JSON String.
     * @param json JSON String
     * @return Application Builder loaded from JSON
     * @throws AssemblyException if unable to declare the assembly
     */
    public static ApplicationBuilder fromJson( String json )
    {
        JsonObject root = Json.createReader( new StringReader( json ) ).readObject();
        return fromJson( root );
    }

    /**
     * Load an ApplicationBuilder from a JSON InputStream.
     * @param json JSON input
     * @return Application Builder loaded from JSON
     * @throws AssemblyException if unable to declare the assembly
     */
    public static ApplicationBuilder fromJson( InputStream json )
        throws AssemblyException
    {
        JsonObject root = Json.createReader( json ).readObject();
        return fromJson( root );
    }

    /**
     * Load an ApplicationBuilder from a JSONObject.
     * @param root JSON object
     * @return Application Builder loaded from JSON
     * @throws AssemblyException if unable to declare the assembly
     */
    public static ApplicationBuilder fromJson( JsonObject root )
        throws AssemblyException
    {
        String applicationName = root.getString( "name" );
        ApplicationBuilder builder = new ApplicationBuilder( applicationName );
        builder.configureWithJson( root );
        return builder;
    }

    /** Configures the application struucture from a JSON document.
     *
     * @param root The JSON document root.
     * @throws AssemblyException if probelms in the Assemblers provided in the JSON document.
     */
    protected void configureWithJson( JsonObject root )
        throws AssemblyException
    {
        JsonValue optLayers = root.get( "layers" );
        if( optLayers != null && optLayers.getValueType() == JsonValue.ValueType.ARRAY )
        {
            JsonArray layers = (JsonArray) optLayers;
            for( int i = 0; i < layers.size(); i++ )
            {
                JsonObject layerObject = layers.getJsonObject( i );
                String layerName = layerObject.getString( "name" );
                LayerDeclaration layerDeclaration = withLayer( layerName );
                JsonValue optUsing = layerObject.get( "uses" );
                if( optUsing != null && optUsing.getValueType() == JsonValue.ValueType.ARRAY )
                {
                    JsonArray using = (JsonArray) optUsing;
                    for( int j = 0; j < using.size(); j++ )
                    {
                        layerDeclaration.using( using.getString( j ) );
                    }
                }
                JsonValue optModules = layerObject.get( "modules" );
                if( optModules != null && optModules.getValueType() == JsonValue.ValueType.ARRAY )
                {
                    JsonArray modules = (JsonArray) optModules;
                    for( int k = 0; k < modules.size(); k++ )
                    {
                        JsonObject moduleObject = modules.getJsonObject( k );
                        String moduleName = moduleObject.getString( "name" );
                        ModuleDeclaration moduleDeclaration = layerDeclaration.withModule( moduleName );
                        JsonValue optAssemblers = moduleObject.get( "assemblers" );
                        if( optAssemblers != null && optAssemblers.getValueType() == JsonValue.ValueType.ARRAY )
                        {
                            JsonArray assemblers = (JsonArray) optAssemblers;
                            for( int m = 0; m < assemblers.size(); m++ )
                            {
                                String string = assemblers.getString( m );
                                moduleDeclaration.withAssembler( string );
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * {@literal main} method that read JSON from STDIN.
     * <p>Passivation exceptions are written to STDERR if any.</p>
     * @param args Unused
     * @throws AssemblyException if the assembly failed
     * @throws ActivationException if the activation failed
     */
    public static void main( String[] args )
        throws ActivationException, AssemblyException
    {
        fromJson( System.in ).withPassivationShutdownHook().newApplication();
    }
}
