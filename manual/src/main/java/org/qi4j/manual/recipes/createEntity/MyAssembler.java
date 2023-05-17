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
package org.qi4j.manual.recipes.createEntity;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.ModuleAssembly;

// START SNIPPET: assembler2
// START SNIPPET: assembler1
public class MyAssembler
    implements Assembler
{
    public void assemble( ModuleAssembly module )
    {
        module.entities( CarEntity.class,
                ManufacturerEntity.class );

        module.values( AccidentValue.class );
// END SNIPPET: assembler1
        module.addServices(
                ManufacturerRepositoryService.class,
                CarEntityFactoryService.class
        ).visibleIn( Visibility.application );
// START SNIPPET: assembler1
    }
}
// END SNIPPET: assembler1
// END SNIPPET: assembler2
