package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import backend.academy.linktracker.scrapper.application.links.usecases.CreateTrackedLinkService;
import backend.academy.linktracker.scrapper.application.links.usecases.ReadTrackedLinkService;
import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.infrastructure.api.OutboxProcessor;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkUpdate;
import backend.academy.linktracker.scrapper.infrastructure.api.updateSenders.LinkUpdateSender;
import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import com.example.notification.RawUpdateEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Slf4j
@TestPropertySource(
        properties = {"spring.jpa.hibernate.ddl-auto=validate", "spring.liquibase.enabled=true", "app.use-queue=true"})
class KafkaProducerTest {

    @Autowired
    private LinkUpdateSender kafkaSender;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private OutboxProcessor outboxProcessor;

    @MockitoBean
    private LinkRepository linkRepository;

    @MockitoBean
    private CreateTrackedLinkService createTrackedLinkService;

    @MockitoBean
    private ReadTrackedLinkService readTrackedLinkService;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", TestcontainersConfiguration.KAFKA::getBootstrapServers);
        registry.add("app.kafka.bootstrap-servers", TestcontainersConfiguration.KAFKA::getBootstrapServers);
        registry.add("spring.datasource.url", TestcontainersConfiguration.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", TestcontainersConfiguration.POSTGRES::getUsername);
        registry.add("spring.datasource.password", TestcontainersConfiguration.POSTGRES::getPassword);

        String schemaRegistryUrl = String.format(
                "http://%s:%d",
                TestcontainersConfiguration.SCHEMA_REGISTRY.getHost(),
                TestcontainersConfiguration.SCHEMA_REGISTRY.getMappedPort(8081));

        registry.add("app.kafka.schema-registry-url", () -> schemaRegistryUrl);
    }

    @Test
    void SendAvroToKafkaTest() throws Exception {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestcontainersConfiguration.KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put("schema.registry.url", kafkaProperties.getSchemaRegistryUrl());
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        try (KafkaConsumer<String, RawUpdateEvent> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(kafkaProperties.getTopicName()));
            LinkUpdate update = new LinkUpdate(1L, "Desc", "author", List.of(123L));
            kafkaSender.send(update);
            outboxProcessor.process();
            RawUpdateEvent receivedEvent = null;
            for (int i = 0; i < 10; i++) {
                ConsumerRecords<String, RawUpdateEvent> records = consumer.poll(Duration.ofSeconds(1));
                if (!records.isEmpty()) {
                    receivedEvent = records.iterator().next().value();
                    break;
                }
            }

            assertNotNull(receivedEvent, "Сообщение не было получено из Kafka");
            assertEquals(1L, receivedEvent.getId());
            assertThat(receivedEvent.getAuthor()).hasToString("author");
        }
    }
}
