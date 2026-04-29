package backend.academy.linktracker.bot.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    public static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");
    public static final RedisContainer REDIS = new RedisContainer(DockerImageName.parse("redis:7-alpine"));
    public static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"))
            .withEnv("KAFKA_HEAP_OPTS", "-Xms256M -Xmx256M")
            .withEnv("KAFKA_LOG_RETENTION_MS", "60000")
            .withEnv("KAFKA_LOG_RETENTION_CHECK_INTERVAL_MS", "5000");
    public static final WireMockServer WIREMOCK =
            new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    static {
        POSTGRES.start();
        REDIS.start();
        KAFKA.start();
        WIREMOCK.start();

        String wiremockUrl = "http://localhost:" + WIREMOCK.port() + "/bot";
        System.setProperty("app.telegram.url", wiremockUrl);
        System.setProperty("wiremock.server.baseUrl", wiremockUrl);

        System.setProperty("spring.kafka.bootstrap-servers", KAFKA.getBootstrapServers());
        System.setProperty("spring.kafka.properties.schema.registry.url", "mock://http://localhost:8081");
        System.setProperty("app.kafka.topic-name", "link_updates");
        System.setProperty("app.use-queue", "true");
    }

    @Bean
    public PostgreSQLContainer postgresContainer() {
        return POSTGRES;
    }

    @Bean
    public RedisContainer redisContainer() {
        return REDIS;
    }

    @Bean
    public KafkaContainer kafkaContainer() {
        return KAFKA;
    }

    @Bean
    public WireMockServer wireMockServer() {
        return WIREMOCK;
    }
}
