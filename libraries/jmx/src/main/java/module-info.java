module org.qi4j.libraries.jmx {
    requires java.management;
    requires org.qi4j.api;
    requires org.qi4j.spi;
    requires org.qi4j.bootstrap;
    requires java.rmi;
    requires slf4j.api;
    exports org.qi4j.library.jmx;
}