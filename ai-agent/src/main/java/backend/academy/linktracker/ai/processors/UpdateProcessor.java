package backend.academy.linktracker.ai.processors;

import com.example.notification.ProcessedUpdateEvent;
import com.example.notification.RawUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateProcessor {

    private final KafkaTemplate<String, ProcessedUpdateEvent> kafkaTemplate;
    private final MessageProcessor messageProcessor;

    @Value("${app.kafka.processed-updates-topic}")
    private String processedTopic;

    @KafkaListener(topics = "${app.kafka.raw-updates-topic}", containerFactory = "kafkaListenerContainerFactory")
    public void onRawUpdate(RawUpdateEvent event) {
        log.info("Received raw update: id={}, author={}", event.getId(), event.getAuthor());
        messageProcessor.process(event).ifPresent(processed -> {
            kafkaTemplate.send(processedTopic, processed);
            log.info("Processed update id={} sent to topic {}", processed.getId(), processedTopic);
        });
    }
}
