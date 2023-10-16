module org.qi4j.libraries.circuitbreaker {
    exports org.qi4j.library.circuitbreaker;
    requires java.desktop;
    requires java.management;
    requires org.qi4j.api;
    requires org.qi4j.libraries.jmx;
    requires slf4j.api;
}