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
package org.qi4j.tools.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObjectBuilder;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.junit.jupiter.api.Test;
import org.qi4j.tools.model.v2.Application;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Visitable Detail Test.
 */
public class VisitableDetailTest
{
    @Test
    public void visit()
        throws AssemblyException, JsonProcessingException
    {
        ApplicationDescriptor application = new Energy4Java().newApplicationModel(
            applicationFactory -> {
                ApplicationAssembly app = applicationFactory.newApplicationAssembly();
                app.setName( "UnderTestApp" );
                app.withActivators( ApplicationActivator.class );

                LayerAssembly layer = app.layer( "LayerName" );
                layer.withActivators( LayerActivator.class );

                ModuleAssembly module = layer.module( "ModuleName" );
                module.withActivators( ModuleActivator.class );

                return app;
            }
        );
        Application detail = new Application( application );
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(detail);
        System.out.println(json);
    }

    public static record AsRecord(int x, int y) { }

    static class ApplicationActivator
        extends ActivatorAdapter<org.qi4j.api.structure.Application>
    {
    }

    static class LayerActivator
        extends ActivatorAdapter<Layer>
    {
    }

    static class ModuleActivator
        extends ActivatorAdapter<Module>
    {
    }
}
