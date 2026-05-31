package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.domain.outboxMessages.OutboxRepository;
import backend.academy.linktracker.scrapper.infrastructure.api.BotClient;
import backend.academy.linktracker.scrapper.infrastructure.api.updateSenders.HttpLinkUpdateSender;
import backend.academy.linktracker.scrapper.infrastructure.api.updateSenders.KafkaLinkUpdateSender;
import backend.academy.linktracker.scrapper.infrastructure.api.updateSenders.LinkUpdateSender;
import backend.academy.linktracker.scrapper.infrastructure.api.updateSenders.ScrapperUpdateSender;
import backend.academy.linktracker.scrapper.properties.AppProperties;
import backend.academy.linktracker.scrapper.properties.KafkaProperties;
import com.example.notification.LinkUpdateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({AppProperties.class, KafkaProperties.class})
public class SenderConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }

    @Bean
    public ProducerFactory<String, LinkUpdateEvent> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        props.put("schema.registry.url", kafkaProperties.getSchemaRegistryUrl());

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, LinkUpdateEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic linkUpdatesTopic() {
        return TopicBuilder.name(kafkaProperties.getTopicName())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean("kafkaLinkUpdateSender")
    public LinkUpdateSender kafkaLinkUpdateSender(OutboxRepository outboxRepository, ObjectMapper objectMapper) {
        return new KafkaLinkUpdateSender(outboxRepository, objectMapper);
    }

    @Bean("httpLinkUpdateSender")
    public LinkUpdateSender httpLinkUpdateSender(BotClient botClient) {
        return new HttpLinkUpdateSender(botClient);
    }

    @Bean
    @Primary
    public LinkUpdateSender scrapperUpdateSender(
            @Qualifier("httpLinkUpdateSender") LinkUpdateSender httpSender,
            @Qualifier("kafkaLinkUpdateSender") LinkUpdateSender kafkaSender,
            AppProperties appProperties) {
        return new ScrapperUpdateSender(httpSender, kafkaSender, appProperties);
    }
}
