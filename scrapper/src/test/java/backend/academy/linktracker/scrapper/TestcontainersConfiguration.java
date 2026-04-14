package backend.academy.linktracker.scrapper;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {
    public static final PostgreSQLContainer POSTGRES =
        new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("scrapper")
            .withUsername("postgres")
            .withPassword("postgres");


    public static final WireMockServer WIREMOCK_SERVER = new WireMockServer(
        WireMockConfiguration.wireMockConfig().dynamicPort()
    );

    @Bean
    @ServiceConnection
    public PostgreSQLContainer postgresContainer() {
        return POSTGRES;
    }

    static {
        WIREMOCK_SERVER.start();
        POSTGRES.start();

        System.setProperty("app.github.url", "http://localhost:" + WIREMOCK_SERVER.port());
        System.setProperty("app.stackoverflow.url", "http://localhost:" + WIREMOCK_SERVER.port());
    }
}
