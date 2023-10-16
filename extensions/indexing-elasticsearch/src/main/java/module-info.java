module org.qi4j.extensions.indexing.elasticsearch {
    requires org.qi4j.bootstrap;
    requires elasticsearch;
    requires org.qi4j.api;
    requires transport;
    requires org.qi4j.libraries.fileconfig;
    requires slf4j.api;
    requires org.qi4j.spi;
    requires java.json;
}