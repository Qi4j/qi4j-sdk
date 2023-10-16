module org.qi4j.libraries.rdf {
    requires rdf4j.model;
    requires org.qi4j.api;
    requires org.qi4j.spi;
    requires java.xml;
    requires rdf4j.repository.api;
    requires rdf4j.repository.sail;
    requires rdf4j.sail.memory;
    requires rdf4j.repository.http;
    requires org.qi4j.libraries.constraints;
    requires org.qi4j.libraries.fileconfig;
    requires rdf4j.sail.nativerdf;
    requires rdf4j.rio.api;
    requires rdf4j.rio.n3;
    requires rdf4j.rio.rdfxml;
    requires rdf4j.rio.turtle;
    exports org.qi4j.library.rdf.repository;
    exports org.qi4j.library.rdf.entity;
}