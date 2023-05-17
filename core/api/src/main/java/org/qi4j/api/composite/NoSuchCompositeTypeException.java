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
package org.qi4j.api.composite;

import java.util.stream.Stream;
import org.qi4j.api.common.InvalidApplicationException;
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.structure.TypeLookup;
import org.qi4j.api.structure.LayerDescriptor;
import org.qi4j.api.structure.ModuleDescriptor;
import org.qi4j.api.structure.TypeLookup;

import static java.util.stream.Collectors.joining;

/**
 * This exception is thrown if client code tries to create a non-existing Composite type.
 */
public abstract class NoSuchCompositeTypeException extends InvalidApplicationException
{
    private static final String NL = System.getProperty( "line.separator" );

    private final String compositeType;
    private final String moduleName;
    private final String visibleTypes;
    private final String metaType;
    private final String candidateTypes;

    protected NoSuchCompositeTypeException( String metaType, String compositeType, ModuleDescriptor module )
    {
        super( NL + "\tCould not find any visible " + metaType + " of type [" + compositeType + "] in module [" + module.name() + "]." );
        this.metaType = metaType;
        this.compositeType = compositeType;
        this.moduleName = module.name();
        visibleTypes = formatVisibleTypes( module.typeLookup() );
        candidateTypes = findCandidateTypes( module );
    }

    public String compositeType()
    {
        return compositeType;
    }

    public String moduleName()
    {
        return moduleName;
    }

    public String visibleTypes()
    {
        return visibleTypes;
    }

    public String candidateTypes()
    {
        return candidateTypes;
    }

    @Override
    public String getMessage()
    {
        return super.getMessage() + NL + candidateTypes + NL + visibleTypes;
    }

    private String formatVisibleTypes( TypeLookup typeLookup )
    {
        return descriptors( typeLookup )
            .map( descriptor ->
                  {
                      String moduleName = descriptor.module().name();
                      String typeName = descriptor.primaryType().getName();
                      return "\t\t[" + typeName + "] in [" + moduleName + "]";
                  } )
            .sorted()
            .distinct()
            .collect( joining( NL, "\tVisible " + metaType + " types are:" + NL, "" ) );
    }

    private String findCandidateTypes( ModuleDescriptor module )
    {
        return Stream.concat( Stream.of( module.layer() ), module.layer().usedLayers().layers() )
                     .flatMap( LayerDescriptor::modules )
                     .map( ModuleDescriptor::typeLookup )
                     .flatMap( this::descriptors )
                     .filter( type -> compositeType.equals( type.primaryType().getName() ) )
                     .map( descriptor ->
                           {
                               Class<?> primarytype = descriptor.primaryType();
                               String typeName = primarytype.getName();
                               return "\t\t[ " + typeName + "] in [" + descriptor.module().name() + "] with visibility " + descriptor.visibility();
                           } )
                     .distinct()
                     .collect( joining( NL, "\tInvisible " + metaType + " types are:" + NL, "" ) );
    }

    protected abstract Stream<? extends CompositeDescriptor> descriptors( TypeLookup typeLookup );

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        NoSuchCompositeTypeException that = (NoSuchCompositeTypeException) o;

        if( !compositeType.equals( that.compositeType ) )
        {
            return false;
        }
        if( !moduleName.equals( that.moduleName ) )
        {
            return false;
        }
        return visibleTypes.equals( that.visibleTypes );
    }

    @Override
    public int hashCode()
    {
        int result = compositeType.hashCode();
        result = 31 * result + moduleName.hashCode();
        result = 31 * result + visibleTypes.hashCode();
        return result;
    }
}
