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
package org.qi4j.bootstrap.layered;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;

import static org.qi4j.api.util.AccessibleObjects.accessible;

public abstract class LayeredApplicationAssembler
    implements ApplicationAssembler
{
    protected final Energy4Java qi4j;
    protected final String name;
    protected final String version;

    private final Application.Mode mode;
    private final HashMap<Class<? extends LayerAssembler>, LayerAssembler> assemblers = new HashMap<>();

    private ApplicationAssembly assembly;
    protected ApplicationDescriptor model;
    protected Application application;

    public LayeredApplicationAssembler( String name, String version, Application.Mode mode )
        throws AssemblyException
    {
        this.name = name;
        this.version = version;
        this.mode = mode;
        qi4j = new Energy4Java();
    }

    public void initialize()
        throws AssemblyException
    {
        model = qi4j.newApplicationModel( this );
        onModelCreated( model );
        instantiateApplication( qi4j, model );
    }

    public ApplicationAssembly assembly()
    {
        return assembly;
    }

    /**
     * This method is called from the <code>initialize</code> method to instantiate the Qi4j application from the
     * application model.
     *
     * <p>
     * The default implementation simply calls;
     * </p>
     * <pre><code>
     *   application = model.newInstance( qi4j.spi() );
     * </code></pre>
     *
     * @param qi4j  The Qi4j runtime engine.
     * @param model The application model descriptor.
     */
    protected void instantiateApplication( Energy4Java qi4j, ApplicationDescriptor model )
    {
        application = model.newInstance( qi4j.spi() );
    }

    /**
     * This method is called after the Application Model has been created, before the instantiation of the Qi4j
     * application.
     *
     * <p>
     * The default implementation does nothing. Applications may have advanced features to inspect or
     * modify the model prior to instantiation, and this is the place where such advanced manipulation is
     * expected to take place.
     * </p>
     *
     * @param model The model that has just been created.
     */
    protected void onModelCreated( ApplicationDescriptor model )
    {
    }

    public ApplicationDescriptor model()
    {
        return model;
    }

    public Application application()
    {
        return application;
    }

    public void start()
        throws ActivationException
    {
        application.activate();
    }

    public void stop()
        throws PassivationException
    {
        application.passivate();
    }

    @Override
    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
    {
        assembly = applicationFactory.newApplicationAssembly();
        assembleApplication();
        return assembly;
    }

    protected void assembleApplication()
    {
        assembly.setName( name );
        assembly.setVersion( version );
        assembly.setMode( mode );
        assembleLayers( assembly );
    }

    protected LayerAssembly createLayer( Class<? extends LayerAssembler> layerAssemblerClass )
        throws IllegalArgumentException
    {
        try
        {
            String classname = layerAssemblerClass.getSimpleName();
            if( classname.endsWith( "Layer" ) )
            {
                classname = classname.substring( 0, classname.length() - 5 ) + " Layer";
            }
            setNameIfPresent( layerAssemblerClass, classname );
            LayerAssembly layer = assembly.layer( classname );

            LayerAssembler layerAssembler = instantiateLayerAssembler( layerAssemblerClass, layer );
            assemblers.put( layerAssemblerClass, layerAssembler );
            assembleLayer( layerAssembler, layer );
            return layer;
        }
        catch( Exception e )
        {
            throw new IllegalArgumentException( "Unable to instantiate layer with " + layerAssemblerClass.getSimpleName(), e );
        }
    }

    protected void assembleLayer( LayerAssembler layerAssembler, LayerAssembly layer )
        throws AssemblyException
    {
        layerAssembler.assemble( layer );
    }

    protected <T extends LayerAssembler> LayerAssembler instantiateLayerAssembler( Class<T> layerAssemblerClass,
                                                                                   LayerAssembly layer
    )
        throws InstantiationException, IllegalAccessException, InvocationTargetException, IllegalLayerAssemblerException
    {
        LayerAssembler assembler = createWithFactoryMethod( layerAssemblerClass, layer );
        if( assembler != null )
        {
            return assembler;
        }
        assembler = createWithConstructor( layerAssemblerClass, layer );
        if( assembler != null )
        {
            return assembler;
        }
        throw new IllegalLayerAssemblerException( "No matching factory method nor constructor found in " + layerAssemblerClass );
    }

    private LayerAssembler createWithFactoryMethod( Class<? extends LayerAssembler> layerAssemblerClass,
                                                    LayerAssembly layer
    )
        throws InvocationTargetException, IllegalAccessException
    {
        try
        {
            Method factoryMethod = accessible( layerAssemblerClass.getDeclaredMethod( "create", LayerAssembly.class ) );
            int modifiers = factoryMethod.getModifiers();
            if( Modifier.isStatic( modifiers ) && LayerAssembler.class.isAssignableFrom( factoryMethod.getReturnType() ) )
            {
                return (LayerAssembler) factoryMethod.invoke( null, layer );
            }
        }
        catch( NoSuchMethodException e )
        {
            try
            {
                Method factoryMethod = accessible( layerAssemblerClass.getDeclaredMethod( "create" ) );
                int modifiers = factoryMethod.getModifiers();
                if( Modifier.isStatic( modifiers ) && LayerAssembler.class.isAssignableFrom( factoryMethod.getReturnType() ) )
                {
                    return (LayerAssembler) factoryMethod.invoke( null );
                }
            }
            catch( NoSuchMethodException e1 )
            {
            }
        }
        return null;
    }

    private LayerAssembler createWithConstructor( Class<? extends LayerAssembler> layerAssemblerClass,
                                                  LayerAssembly assembly
    )
        throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        try
        {
            Constructor<? extends LayerAssembler> constructor = layerAssemblerClass.getConstructor( LayerAssembly.class );
            if( constructor != null )
            {
                return accessible( constructor ).newInstance( assembly );
            }
        }
        catch( NoSuchMethodException e )
        {
            try
            {
                Constructor<? extends LayerAssembler> constructor = layerAssemblerClass.getDeclaredConstructor();
                if( constructor != null )
                {
                    return accessible( constructor ).newInstance();
                }
            }
            catch( NoSuchMethodException e1 )
            {
                return null;
            }
        }
        return null;
    }

    static void setNameIfPresent( Class<?> clazz, String classname )
        throws IllegalAccessException
    {
        try
        {
            Field field = clazz.getDeclaredField( "NAME" );
            if( Modifier.isStatic( field.getModifiers() ) )
            {
                accessible( field ).set( null, classname );
            }
        }
        catch( Exception e )
        {
            // Ignore and consider normal.
        }
    }

    @SuppressWarnings( "unchecked" )
    protected <T extends LayerAssembler> T assemblerOf( Class<T> layerAssemblerClass )
    {
        return (T) assemblers.get( layerAssemblerClass );
    }

    /**
     * Called from the <code>assemble</code> method to assemble the layers in the application.
     *
     * <p>
     * This method must be implemented, and is typically a list of LayerAssembler instantiations, followed
     * by {@link LayerAssembly#uses(LayerAssembly...)} declarations.
     * </p>
     *
     * @param assembly Application assembly
     * @throws AssemblyException on invalid assembly
     */
    protected abstract void assembleLayers( ApplicationAssembly assembly )
        throws AssemblyException;
}
