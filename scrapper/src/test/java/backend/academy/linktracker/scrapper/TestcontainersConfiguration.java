package backend.academy.linktracker.scrapper;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    public static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("scrapper")
            .withUsername("postgres")
            .withPassword("postgres");

    public static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:3.7.0")
            .withEnv("KAFKA_HEAP_OPTS", "-Xms256M -Xmx256M")
            .withEnv("KAFKA_LOG_RETENTION_MS", "60000")
            .withEnv("KAFKA_LOG_RETENTION_CHECK_INTERVAL_MS", "5000");

    static {
        POSTGRES.start();
        KAFKA.start();

        System.setProperty("spring.kafka.bootstrap-servers", KAFKA.getBootstrapServers());
        String mockRegistryUrl = "mock://http://localhost:8081";
        System.setProperty("spring.kafka.properties.schema.registry.url", mockRegistryUrl);
        System.setProperty("schema.registry.url", mockRegistryUrl);

        System.setProperty("app.use-queue", "true");
        System.setProperty("app.kafka.topic-name", "link_updates");
    }

    @Bean
    @ServiceConnection
    public PostgreSQLContainer postgresContainer() {
        return POSTGRES;
    }

    @Bean
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        return KAFKA;
    }
}
