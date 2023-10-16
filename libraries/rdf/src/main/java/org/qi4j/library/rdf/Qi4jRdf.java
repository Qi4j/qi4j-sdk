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
 * This is the RDF vocabulary for Qi4j.
 */
public interface Qi4jRdf
{

    // MODEL
    // Namespace TODO: Need to figure out what these should really be!
    String QI4J_MODEL = "http://qi4j.org/rdf/model/1.0/";
    String QI4J_MODEL_TYPES = "http://qi4j.org/rdf/model/1.0/type#";
    String QI4J_MODEL_RELATIONSHIPS = "http://qi4j.org/rdf/module/1.0/";
    String QI4J_MODEL_PROPERTIES = "http://qi4j.org/rdf/model/1.0/property#";

    // Types
    IRI TYPE_APPLICATION = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "application" );
    IRI TYPE_LAYER = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "layer" );
    IRI TYPE_MODULE = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "module" );
    IRI TYPE_ENTITY = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "entity" );
    IRI TYPE_QUALIFIER = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "qualifier" );
    IRI TYPE_COMPOSITE = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "composite" );
    IRI TYPE_SERVICE = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "service" );
    IRI TYPE_METHOD = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "method" );
    IRI TYPE_CONSTRAINT = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "constraint" );
    IRI TYPE_CONCERN = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "concern" );
    IRI TYPE_CONSTRUCTOR = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "constructor" );
    IRI TYPE_SIDEEFFECT = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "sideeffect" );
    IRI TYPE_MIXIN = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "mixin" );
    IRI TYPE_FIELD = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "field" );
    IRI TYPE_CLASS = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "class" );
    IRI TYPE_OBJECT = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "object" );
    IRI TYPE_PARAMETER = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "parameter" );
    IRI TYPE_INJECTION = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "injection" );
    IRI TYPE_INFO = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_TYPES + "info" );

    // Properties
    IRI HAS_INJECTIONS = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_PROPERTIES + "hasinjections" );

    // Relationship
    IRI RELATIONSHIP_COMPOSITE = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "composite" );
    IRI RELATIONSHIP_ENTITY = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "entity" );
    IRI RELATIONSHIP_SERVICE = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "service" );
    IRI RELATIONSHIP_OBJECT = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "object" );
    IRI RELATIONSHIP_PRIVATE_METHOD = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "private/method" );
    IRI RELATIONSHIP_INJECTION = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "injection" );
    IRI RELATIONSHIP_CONSTRUCTOR = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "constructor" );
    IRI RELATIONSHIP_FIELD = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "field" );
    IRI RELATIONSHIP_APPLIESTO = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "appliesto" );
    IRI RELATIONSHIP_METHOD = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "method" );
    IRI RELATIONSHIP_CONSTRAINT = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "constraint" );
    IRI RELATIONSHIP_CONCERN = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "concern" );
    IRI RELATIONSHIP_SIDEEFFECT = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "sideeffect" );
    IRI RELATIONSHIP_PUBLIC_SERVICE = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "public/service" );
    IRI RELATIONSHIP_PRIVATE_SERVICE = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "private/service" );
    IRI RELATIONSHIP_PROVIDEDBY = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "providedby" );
    IRI RELATIONSHIP_SERVICEINFO = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "info/service" );
    IRI RELATIONSHIP_INFOVALUE = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "info/value" );
    IRI RELATIONSHIP_MIXIN = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "mixin" );
    IRI RELATIONSHIP_LAYER = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "layer" );
    IRI RELATIONSHIP_MODULE = SimpleValueFactory.getInstance().createIRI( QI4J_MODEL_RELATIONSHIPS + "module" );
}
