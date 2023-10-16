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

package org.qi4j.library.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * This is the RDF vocabulary for Qi4j Entity data.
 */
public interface Qi4jEntity
{
    // Model
    String NAMESPACE = "http://qi4j.org/rdf/entity/1.0/";

    // Types
    IRI ENTITY = SimpleValueFactory.getInstance().createIRI(NAMESPACE + "entity" );
    IRI ENTITYTYPEREFERENCE = SimpleValueFactory.getInstance().createIRI( NAMESPACE + "entitytypereference" );
    IRI QUALIFIER = SimpleValueFactory.getInstance().createIRI( NAMESPACE + "qualifier" );
}
