package backend.academy.linktracker.ai;

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
            registry.add("app.kafka.raw-updates-topic", () -> "link.raw-updates");
            registry.add("app.kafka.processed-updates-topic", () -> "link.processed-updates");
            registry.add("app.kafka.consumer-group", () -> "test-group-" + java.util.UUID.randomUUID());
            registry.add("app.kafka.schema-registry-url", () -> "mock://http://localhost:8081");

            registry.add("ai-agent.filtering.stop-words", () -> "spam,ads,promo");
            registry.add("ai-agent.filtering.excluded-authors", () -> "bot-user");
            registry.add("ai-agent.filtering.min-length", () -> "20");
            registry.add("ai-agent.summarization.threshold", () -> "500");
            registry.add("ai-agent.processor.type", () -> "simple");
        };
    }
}
