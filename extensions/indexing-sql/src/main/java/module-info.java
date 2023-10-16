module org.qi4j.extensions.indexing.sql {
    exports org.qi4j.index.sql.assembly;
    requires org.qi4j.api;
    requires org.qi4j.bootstrap;
    requires org.qi4j.libraries.sql.generator;
    requires org.qi4j.extensions.reindexer;
    requires java.sql;
    requires org.qi4j.spi;
    requires org.qi4j.libraries.sql;
    requires slf4j.api;
}