package br.com.gabryel.waes.rdc.banking.it;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainersExtension implements BeforeAllCallback, AfterAllCallback {

    private PostgreSQLContainer<?> postgreSQLContainer;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.0-alpine")
            .withDatabaseName("testdb")
            .withUsername("sa")
            .withPassword("password");

        postgreSQLContainer.start();

        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.flyway.url", postgreSQLContainer.getJdbcUrl());
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        postgreSQLContainer.stop();
    }
}