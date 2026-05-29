package backend.academy.linktracker.ai;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

import backend.academy.linktracker.ai.processors.GroupingService;
import backend.academy.linktracker.ai.properties.AppProperties;
import backend.academy.linktracker.ai.properties.KafkaProperties;
import com.example.notification.ProcessedUpdateEvent;
import com.example.notification.RawUpdateEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class KafkaIntegrationTest {

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private GroupingService groupingService;

    private AppProperties properties;

    @Test
    void shouldGroupUpdatesAndPublishToKafka() throws Exception {
        long chatId = 111L;

        try (KafkaProducer<String, RawUpdateEvent> producer = createRawProducer()) {
            producer.send(new ProducerRecord<>("link.raw-updates", createRaw(1, "Regular", chatId)))
                    .get();
            producer.send(new ProducerRecord<>("link.raw-updates", createRaw(2, "critical bug", chatId)))
                    .get();
        }

        try (var consumer = createProcessedConsumer()) {
            consumer.subscribe(List.of("link.processed-updates"));

            Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
                var records = consumer.poll(Duration.ofMillis(500));
                boolean found = StreamSupport.stream(records.spliterator(), false)
                        .anyMatch(r -> r.value().getTgChatIds().contains(chatId)
                                && r.value().getPriority().toString().equals("HIGH"));

                assertTrue(found);
            });
        }
    }

    @Test
    void shouldNotPublishFilteredMessage() throws Exception {
        long chatId = 999L;
        try (KafkaProducer<String, RawUpdateEvent> producer = createRawProducer()) {
            producer.send(new ProducerRecord<>("link.raw-updates", createRaw(3, "some spam", chatId)))
                    .get();
        }

        try (KafkaConsumer<String, ProcessedUpdateEvent> consumer = createProcessedConsumer()) {
            consumer.subscribe(Collections.singletonList("link.processed-updates"));
            ConsumerRecords<String, ProcessedUpdateEvent> records = consumer.poll(Duration.ofSeconds(3));

            boolean found = false;
            for (var record : records) {
                if (record.value().getTgChatIds().contains(chatId)) {
                    found = true;
                    break;
                }
            }
            assertFalse(found);
        }
    }

    private KafkaProducer<String, RawUpdateEvent> createRawProducer() {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, TestcontainersConfiguration.KAFKA.getBootstrapServers());
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        p.put("schema.registry.url", "mock://http://localhost:8081");
        return new KafkaProducer<>(p);
    }

    private KafkaConsumer<String, ProcessedUpdateEvent> createProcessedConsumer() {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestcontainersConfiguration.KAFKA.getBootstrapServers());
        p.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID());
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        p.put("schema.registry.url", "mock://http://localhost:8081");
        p.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        return new KafkaConsumer<>(p);
    }

    private RawUpdateEvent createRaw(long id, String desc, long chatId) {
        return RawUpdateEvent.newBuilder()
                .setId(id)
                .setDescription(desc)
                .setAuthor("nick")
                .setTgChatIds(List.of(chatId))
                .build();
    }
}
