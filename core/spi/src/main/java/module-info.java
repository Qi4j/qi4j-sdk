module org.qi4j.spi {
    requires org.qi4j.api;
    requires java.json;
    requires java.xml;

    exports org.qi4j.serialization.javaxjson;
    exports org.qi4j.entitystore.memory;
    exports org.qi4j.spi;
    exports org.qi4j.spi.cache;
    exports org.qi4j.spi.entity;
    exports org.qi4j.spi.entitystore;
    exports org.qi4j.spi.entitystore.helpers;
    exports org.qi4j.spi.metrics;
    exports org.qi4j.spi.messaging;
    exports org.qi4j.spi.module;
    exports org.qi4j.spi.query;
    exports org.qi4j.spi.serialization;
    exports org.qi4j.spi.type;
    exports org.qi4j.spi.util;
}
