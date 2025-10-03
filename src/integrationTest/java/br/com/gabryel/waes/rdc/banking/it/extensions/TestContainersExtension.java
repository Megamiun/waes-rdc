package br.com.gabryel.waes.rdc.banking.it.extensions;

import br.com.gabryel.waes.rdc.banking.repository.*;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

public class TestContainersExtension implements BeforeAllCallback, AfterEachCallback {

    private static PostgreSQLContainer<?> postgreSQLContainer;

    private static final List<Class<? extends JpaRepository<?, ?>>> deletionOrder = List.of(
        LedgerEntryRepository.class,
        TransactionTransferRepository.class,
        TransactionRepository.class,
        CardRepository.class,
        AccountDocumentRepository.class,
        AccountRepository.class
    );

    /**
     * Starts a shared Postgres TestContainer on first extension test.
     *
     * @param context the current extension context
     */
    @Override
    public synchronized void beforeAll(ExtensionContext context) {
        if (postgreSQLContainer != null)
            return;

        postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.0-alpine")
            .withDatabaseName("testdb")
            .withUsername("sa")
            .withPassword("password")
            // The container will be terminated some moments after JVM shutdown now
            .withReuse(false);

        postgreSQLContainer.start();

        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.flyway.url", postgreSQLContainer.getJdbcUrl());
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        var springContext = SpringExtension.getApplicationContext(context);

        deletionOrder.forEach(repository -> springContext.getBean(repository).deleteAll());
    }
}