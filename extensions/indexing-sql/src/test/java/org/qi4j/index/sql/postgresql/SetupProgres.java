package org.qi4j.index.sql.postgresql;

import org.testcontainers.containers.PostgreSQLContainer;

class SetupProgres
{
    static PostgreSQLContainer newContainer()
    {
        //noinspection resource
        PostgreSQLContainer postgres = (PostgreSQLContainer) new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("jdbc_test_db")
            .withReuse(true);
        postgres.withUsername("jdbc_test_login")
            .withPassword("password")
            .withDatabaseName("jdbc_test_db");
        postgres.withInitScript("init-postgres.sql");
        return postgres;
    }
}
