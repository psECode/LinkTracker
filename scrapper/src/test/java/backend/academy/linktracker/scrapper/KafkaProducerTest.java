package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkUpdate;
import backend.academy.linktracker.scrapper.infrastructure.api.updateSenders.LinkUpdateSender;
import com.example.notification.LinkUpdateEvent;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class KafkaProducerTest {

    @Autowired
    private LinkUpdateSender kafkaSender;

    @Value("${app.kafka.topic-name}")
    private String topicName;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Test
    void SendAvroToKafkaTest() throws Exception {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + System.currentTimeMillis());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);

        props.put("schema.registry.url", "mock://http://localhost:8081");
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        try (KafkaConsumer<String, LinkUpdateEvent> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topicName));

            LinkUpdate update = new LinkUpdate(1L, URI.create("https://test.com"), "Desc", List.of(123L));
            kafkaSender.send(update);

            // пришлось сделать цикл так как кафка не успевает разогреться
            LinkUpdateEvent receivedEvent = null;
            for (int i = 0; i < 15; i++) {
                ConsumerRecords<String, LinkUpdateEvent> records = consumer.poll(Duration.ofSeconds(1));
                if (!records.isEmpty()) {
                    receivedEvent = records.iterator().next().value();
                    break;
                }
            }

            assertNotNull(receivedEvent);
            assertEquals(1L, receivedEvent.getId());
            assertEquals("https://test.com", receivedEvent.getUrl().toString());
        }
    }
}
