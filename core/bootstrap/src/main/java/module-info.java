module org.qi4j.bootstrap {
    requires org.qi4j.api;
    requires org.qi4j.spi;
    requires java.json;

    exports org.qi4j.bootstrap;
    exports org.qi4j.bootstrap.layered;
    exports org.qi4j.bootstrap.defaults;
}
