module org.qi4j.extensions.entitystore.redis {
    requires org.qi4j.bootstrap;
    requires redis.clients.jedis;
    requires org.qi4j.api;
    requires org.qi4j.spi;
    requires org.qi4j.libraries.locking;
}