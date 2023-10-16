module org.qi4j.api {
    requires java.logging;
    exports org.qi4j.api;
    exports org.qi4j.api.serialization;
    exports org.qi4j.api.identity;
    exports org.qi4j.api.sideeffect;
    exports org.qi4j.api.sideeffect.internal;
    exports org.qi4j.api.object;
    exports org.qi4j.api.metrics;
    exports org.qi4j.api.configuration;
    exports org.qi4j.api.activation;
    exports org.qi4j.api.common;
    exports org.qi4j.api.entity;
    exports org.qi4j.api.indexing;
    exports org.qi4j.api.constraint;
    exports org.qi4j.api.structure;
    exports org.qi4j.api.composite;
    exports org.qi4j.api.value;
    exports org.qi4j.api.concern;
    exports org.qi4j.api.concern.internal;
    exports org.qi4j.api.unitofwork;
    exports org.qi4j.api.unitofwork.concern;
    exports org.qi4j.api.mixin;
    exports org.qi4j.api.cache;
    exports org.qi4j.api.usecase;
    exports org.qi4j.api.time;
    exports org.qi4j.api.association;
    exports org.qi4j.api.service;
    exports org.qi4j.api.service.qualifier;
    exports org.qi4j.api.service.importer;
    exports org.qi4j.api.property;
    exports org.qi4j.api.util;
    exports org.qi4j.api.type;
    exports org.qi4j.api.query;
    exports org.qi4j.api.query.grammar;
    exports org.qi4j.api.injection;
    exports org.qi4j.api.injection.scope;
    exports org.qi4j.api.messaging;
}