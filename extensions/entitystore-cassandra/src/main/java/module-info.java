module org.qi4j.extensions.entitystore.cassandra {
    requires org.qi4j.bootstrap;
    requires cassandra.driver.core;
    requires org.qi4j.api;
    requires org.qi4j.spi;
    requires org.qi4j.libraries.constraints;
    requires jackson.databind;
    requires org.qi4j.libraries.locking;
}