module org.qi4j.testsupport {
    exports org.qi4j.test;
    requires org.qi4j.api;
    requires org.qi4j.spi;
    requires org.qi4j.bootstrap;
    requires org.hamcrest;
    requires java.desktop;
    requires java.management;
    requires org.junit.jupiter.api;
    requires org.junit.platform.commons;
}
