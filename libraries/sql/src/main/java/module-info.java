module org.qi4j.libraries.sql {
    requires org.qi4j.api;
    requires org.qi4j.bootstrap;
    requires org.qi4j.libraries.circuitbreaker;
    requires java.sql;
    requires slf4j.api;
    requires org.qi4j.spi;
    requires java.management;
    exports org.qi4j.library.sql.common;
    exports org.qi4j.library.sql.assembly;
    exports org.qi4j.library.sql.datasource;
}