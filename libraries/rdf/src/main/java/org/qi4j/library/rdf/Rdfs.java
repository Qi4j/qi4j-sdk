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

public interface Rdfs
{
    String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    IRI ID = SimpleValueFactory.getInstance().createIRI(RDF + "ID" );

    // Classes
    IRI RESOURCE = SimpleValueFactory.getInstance().createIRI( RDF + "resource" );
    IRI LITERAL = SimpleValueFactory.getInstance().createIRI( RDFS + "Literal" );
    IRI XML_LITERAL = SimpleValueFactory.getInstance().createIRI( RDF + "XMLLiteral" );
    IRI CLASS = SimpleValueFactory.getInstance().createIRI( RDFS + "Class" );
    IRI PROPERTY = SimpleValueFactory.getInstance().createIRI( RDF + "Property" );
    IRI DATATYPE = SimpleValueFactory.getInstance().createIRI( RDFS + "Datatype" );
    IRI STATEMENT = SimpleValueFactory.getInstance().createIRI( RDF + "Statement" );
    IRI BAG = SimpleValueFactory.getInstance().createIRI( RDF + "Bag" );
    IRI SEQ = SimpleValueFactory.getInstance().createIRI( RDF + "Seq" );
    IRI ALT = SimpleValueFactory.getInstance().createIRI( RDF + "Alt" );
    IRI CONTAINER = SimpleValueFactory.getInstance().createIRI( RDFS + "Container" );
    IRI CONTAINER_MEMBERSHIP_PROPERTY = SimpleValueFactory.getInstance().createIRI( RDFS + "ContainerMembershipProperty" );
    IRI LIST = SimpleValueFactory.getInstance().createIRI( RDF + "List" );
    IRI LIST_ITEM = SimpleValueFactory.getInstance().createIRI( RDF + "li" );

    // Properties
    IRI TYPE = SimpleValueFactory.getInstance().createIRI( RDF + "type" );
    IRI SUB_CLASS_OF = SimpleValueFactory.getInstance().createIRI( RDFS + "subClassOf" );
    IRI SUB_PROPERTY_OF = SimpleValueFactory.getInstance().createIRI( RDFS + "subPropertyOf" );
    IRI DOMAIN = SimpleValueFactory.getInstance().createIRI( RDFS + "domain" );
    IRI RANGE = SimpleValueFactory.getInstance().createIRI( RDFS + "range" );
    IRI LABEL = SimpleValueFactory.getInstance().createIRI( RDFS + "label" );
    IRI COMMENT = SimpleValueFactory.getInstance().createIRI( RDFS + "comment" );
    IRI MEMBER = SimpleValueFactory.getInstance().createIRI( RDFS + "member" );
    IRI FIRST = SimpleValueFactory.getInstance().createIRI( RDF + "first" );
    IRI REST = SimpleValueFactory.getInstance().createIRI( RDF + "rest" );
    IRI SEE_ALSO = SimpleValueFactory.getInstance().createIRI( RDFS + "seeAlso" );
    IRI IS_DEFINED_BY = SimpleValueFactory.getInstance().createIRI( RDFS + "isDefinedBy" );
    IRI VALUE = SimpleValueFactory.getInstance().createIRI( RDF + "value" );
    IRI SUBJECT = SimpleValueFactory.getInstance().createIRI( RDF + "subject" );
    IRI PREDICATE = SimpleValueFactory.getInstance().createIRI( RDF + "predicate" );
    IRI OBJECT = SimpleValueFactory.getInstance().createIRI( RDF + "object" );

}
