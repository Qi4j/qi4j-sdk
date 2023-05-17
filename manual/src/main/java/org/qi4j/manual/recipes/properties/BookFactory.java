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
package org.qi4j.manual.recipes.properties;

import org.qi4j.api.composite.TransientBuilder;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;

public class BookFactory
{
// START SNIPPET: create
    @Structure
    Module module;
// END SNIPPET: create

    public Book create()
    {
// START SNIPPET: create
        TransientBuilder<Book> builder = module.newTransientBuilder( Book.class );
        Book prototype = builder.prototype();
        prototype.title().set( "The Death of POJOs" );
        prototype.author().set( "Niclas Hedhman" );
        Book book = builder.newInstance();
        String title = book.title().get();     // Retrieves the title.
        book.title().set( "Long Live POJOs" ); // throws an IllegalStateException
// END SNIPPET: create
        return book;
    }
}
