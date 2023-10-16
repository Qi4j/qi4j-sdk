module org.qi4j.samples.sql.support {
    requires org.qi4j.api;
    requires org.qi4j.bootstrap;
    requires org.qi4j.spi;
    requires org.qi4j.extensions.entitystore.sqlkv;
    requires org.qi4j.extensions.indexing.sql;
    requires org.qi4j.libraries.sql;
    requires org.qi4j.libraries.sql.dbcp;
    requires java.sql;
}