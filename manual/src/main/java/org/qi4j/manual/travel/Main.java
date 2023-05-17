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
package org.qi4j.manual.travel;

import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

public class Main
{

    public static void main(String[] args) throws Exception
    {
        SingletonAssembler singleton = new SingletonAssembler()
        {
            // START SNIPPET: assemble
            @Override
            public void assemble(ModuleAssembly module)
            {
                module.addServices(TravelPlanService.class)
                        .instantiateOnStartup()
                        .identifiedBy("ExpediaService");

                module.addServices(TravelPlanService.class)
                        .instantiateOnStartup()
                        .identifiedBy("OrbitzService");
            }
            // END SNIPPET: assemble
        };
    }

    public static void simple(String[] args) throws Exception
    {
        SingletonAssembler singleton = new SingletonAssembler()
        {
            // START SNIPPET: simple
            @Override
            public void assemble(ModuleAssembly module)
            {
                module.addServices(TravelPlanService.class).instantiateOnStartup();
            }
            // END SNIPPET: simple
        };
    }

}
