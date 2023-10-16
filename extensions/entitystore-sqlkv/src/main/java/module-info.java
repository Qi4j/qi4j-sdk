module org.qi4j.extensions.entitystore.sqlkv {
    exports org.qi4j.entitystore.sqlkv.assembly;
    requires org.qi4j.api;
    requires org.qi4j.bootstrap;
    requires org.jooq;
    requires org.qi4j.libraries.sql;
    requires java.sql;
    requires org.qi4j.spi;
}