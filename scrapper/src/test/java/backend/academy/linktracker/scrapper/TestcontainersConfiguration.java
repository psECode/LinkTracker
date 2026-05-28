package backend.academy.linktracker.scrapper;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Profile("test")
public class TestcontainersConfiguration {

    static final Network NETWORK = Network.newNetwork();

    public static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    public static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"))
            .withNetwork(NETWORK)
            .withNetworkAliases("kafka");

    public static final WireMockServer WIREMOCK =
            new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    public static final GenericContainer<?> SCHEMA_REGISTRY = new GenericContainer<>(
                    DockerImageName.parse("confluentinc/cp-schema-registry:7.5.0"))
            .withNetwork(NETWORK)
            .withExposedPorts(8081)
            .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
            .withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
            .withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9093")
            .waitingFor(Wait.forHttp("/subjects").forStatusCode(200))
            .dependsOn(KAFKA);

    static {
        POSTGRES.start();
        KAFKA.start();
        WIREMOCK.start();
        SCHEMA_REGISTRY.start();
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
    public WireMockServer wireMockServer() {
        return WIREMOCK;
    }

    @Bean
    public DynamicPropertyRegistrar dynamicPropertyRegistrar() {
        return (registry) -> {
            registry.add("app.bot.url", () -> "http://localhost:" + WIREMOCK.port());

            registry.add("app.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
            registry.add("app.kafka.schema-registry-url", () -> "mock://http://localhost:8081");
            registry.add("app.kafka.topic-name", () -> "link_updates");
            registry.add("app.kafka.use-queue", () -> "true");

            registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
            registry.add("spring.liquibase.enabled", () -> "false");
        };
    }
}
