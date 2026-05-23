package backend.academy.linktracker.ai;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.ai.processors.UpdateProcessor;
import com.example.notification.ProcessedUpdateEvent;
import com.example.notification.RawUpdateEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class KafkaIntegrationTest {

    @Value("${app.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.raw-updates-topic}")
    private String rawUpdatesTopic;

    @Value("${app.kafka.processed-updates-topic}")
    private String processedUpdatesTopic;

    @Value("${app.kafka.schema-registry-url}")
    private String schemaRegistryUrl;

    @Autowired
    private UpdateProcessor processor;

    @Test
    void successfulProcessingAndSendingTest() throws Exception {
        long id = 1001L;
        RawUpdateEvent raw = RawUpdateEvent.newBuilder()
                .setId(id)
                .setDescription("description                    ")
                .setAuthor("nick")
                .setTgChatIds(List.of(111L, 222L))
                .build();

        try (KafkaProducer<String, RawUpdateEvent> producer = createRawProducer()) {
            producer.send(new ProducerRecord<>(rawUpdatesTopic, raw)).get();
        }

        ProcessedUpdateEvent received = waitForProcessedMessage(id);

        assertThat(received).isNotNull();
        assertThat(received.getId()).isEqualTo(id);
        assertThat(received.getDescription()).hasToString("description                    ");
        assertThat(received.getTgChatIds()).containsExactly(111L, 222L);
        assertThat(received.getPriority()).hasToString("LOW");
    }

    @Test
    void testFilteringByStopWord() throws Exception {
        long id = 2001L;
        RawUpdateEvent raw = RawUpdateEvent.newBuilder()
                .setId(id)
                .setDescription("description with spam")
                .setAuthor("nick")
                .setTgChatIds(List.of(111L))
                .build();

        try (KafkaProducer<String, RawUpdateEvent> producer = createRawProducer()) {
            producer.send(new ProducerRecord<>(rawUpdatesTopic, raw)).get();
        }

        ProcessedUpdateEvent received = waitForProcessedMessage(id);
        assertThat(received).isNull();
    }

    @Test
    void testSummarizationTriggered() throws Exception {
        long id = 3001L;
        String text = "A".repeat(600);
        RawUpdateEvent raw = RawUpdateEvent.newBuilder()
                .setId(id)
                .setDescription(text)
                .setAuthor("nick")
                .setTgChatIds(List.of(333L))
                .build();

        try (KafkaProducer<String, RawUpdateEvent> producer = createRawProducer()) {
            producer.send(new ProducerRecord<>(rawUpdatesTopic, raw)).get();
        }

        ProcessedUpdateEvent received = waitForProcessedMessage(id);
        assertThat(received).isNotNull();
        assertThat(received.getDescription()).hasSize(503);
        assertThat(received.getDescription()).endsWith("...");
    }

    private KafkaProducer<String, RawUpdateEvent> createRawProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", schemaRegistryUrl);
        return new KafkaProducer<>(props);
    }

    private ProcessedUpdateEvent waitForProcessedMessage(long expectedId) {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProps.put("schema.registry.url", schemaRegistryUrl);
        consumerProps.put("specific.avro.reader", "true");

        try (KafkaConsumer<String, ProcessedUpdateEvent> consumer = new KafkaConsumer<>(consumerProps)) {
            consumer.subscribe(Collections.singletonList(processedUpdatesTopic));
            AtomicReference<ProcessedUpdateEvent> result = new AtomicReference<>();
            for (int i = 0; i < 30; i++) {
                ConsumerRecords<String, ProcessedUpdateEvent> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, ProcessedUpdateEvent> record : records) {
                    if (record.value().getId() == expectedId) {
                        result.set(record.value());
                        break;
                    }
                }
                if (result.get() != null) break;
            }
            return result.get();
        }
    }
}
