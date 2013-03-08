/*
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.qi4j.library.rest.admin;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class RestAssembler
    implements Assembler
{
    private Visibility visibility;

    public RestAssembler()
    {
        this( Visibility.application );
    }

    public RestAssembler( Visibility visibility )
    {
        this.visibility = visibility;
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.objects( RestApplication.class ).visibleIn( visibility );
        module.objects( Qi4jFinder.class,
                        EntitiesResource.class,
                        EntityResource.class,
                        IndexResource.class,
                        SPARQLResource.class );
    }
}
