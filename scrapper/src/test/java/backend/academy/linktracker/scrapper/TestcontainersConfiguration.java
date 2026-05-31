package backend.academy.linktracker.scrapper;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    public static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");
    public static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

    static {
        POSTGRES.start();
        KAFKA.start();
    }

    @Bean
    @ServiceConnection
    public PostgreSQLContainer postgresContainer() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        return POSTGRES;
    }

    @Bean
    @ServiceConnection
    public KafkaContainer kafkaContainer() {
        if (!KAFKA.isRunning()) {
            KAFKA.start();
        }
        return KAFKA;
    }

    @Bean
    public DynamicPropertyRegistrar dynamicPropertyRegistrar() {
        return (registry) -> {
            registry.add("app.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
            registry.add("app.kafka.schema-registry-url", () -> "mock://http://localhost:8081");
            registry.add("app.kafka.topic-name", () -> "link_updates");
            registry.add("app.kafka.use-queue", () -> "true");

            registry.add("app.access-type", () -> "jpa");
            registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
            registry.add("spring.liquibase.enabled", () -> "false");
        };
    }
}
