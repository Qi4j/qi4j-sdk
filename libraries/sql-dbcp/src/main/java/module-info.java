module org.qi4j.libraries.sql.dbcp {
    exports org.qi4j.library.sql.dbcp;
    requires org.qi4j.api;
    requires org.qi4j.bootstrap;
    requires org.qi4j.libraries.sql;
    requires commons.dbcp2;
    requires java.sql;
}