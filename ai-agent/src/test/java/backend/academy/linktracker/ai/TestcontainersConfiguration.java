package backend.academy.linktracker.ai;

import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    public static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

    static {
        KAFKA.start();
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
        return registry -> {
            registry.add("app.kafka.bootstrap-servers", KAFKA::getBootstrapServers);

            registry.add("ai-agent.prioritization.high-keywords", () -> List.of("critical", "urgent", "security"));
            registry.add("ai-agent.prioritization.low-keywords", () -> List.of("typo", "docs"));
            registry.add("app.kafka.schema-registry-url", () -> "mock://http://localhost:8081");

            registry.add("ai-agent.grouping.window-ms", () -> "1000");

            registry.add("ai-agent.filtering.stop-words", () -> "spam,ads");
            registry.add("ai-agent.filtering.min-length", () -> "10");
            registry.add("ai-agent.summarization.threshold", () -> "500");
        };
    }
}
