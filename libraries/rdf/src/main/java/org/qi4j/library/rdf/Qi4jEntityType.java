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
 * This is the RDF vocabulary for Qi4j EntityType data.
 */
public interface Qi4jEntityType
{
    // Namespace
    String NAMESPACE = "http://qi4j.org/rdf/entitytype/1.0/";

    // Predicates
    IRI QUERYABLE = SimpleValueFactory.getInstance().createIRI( NAMESPACE + "queryable" );
    IRI VERSION = SimpleValueFactory.getInstance().createIRI( NAMESPACE + "version" );
    IRI TYPE = SimpleValueFactory.getInstance().createIRI( NAMESPACE + "type" );
}