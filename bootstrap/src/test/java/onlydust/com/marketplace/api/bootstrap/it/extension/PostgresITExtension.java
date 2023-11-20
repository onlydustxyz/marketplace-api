package onlydust.com.marketplace.api.bootstrap.it.extension;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

public class PostgresITExtension implements BeforeAllCallback, AfterAllCallback {

    PostgreSQLContainer postgresSQLContainer =
            new PostgreSQLContainer<>("postgres:14.3-alpine")
                    .withDatabaseName("marketplace_db")
                    .withUsername("test")
                    .withPassword("test")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("/staging_db/dump"), "/tmp")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("/staging_db/scripts"), "/docker-entrypoint-initdb.d")
                    .waitingFor(Wait.forLogMessage(".*PostgreSQL init process complete; ready for start up.*", 1));


    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        postgresSQLContainer.start();
        System.setProperty("spring.datasource.url", postgresSQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgresSQLContainer.getUsername());
        System.setProperty("spring.datasource.password", postgresSQLContainer.getPassword());
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        postgresSQLContainer.stop();
    }
}
