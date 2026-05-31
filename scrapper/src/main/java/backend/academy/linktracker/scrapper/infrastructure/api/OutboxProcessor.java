package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.domain.outboxMessages.OutboxMessage;
import backend.academy.linktracker.scrapper.domain.outboxMessages.OutboxRepository;
import backend.academy.linktracker.scrapper.domain.outboxMessages.OutboxStatus;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkUpdate;
import com.example.notification.LinkUpdateEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, LinkUpdateEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic-name}")
    private String topicName;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void process() {
        List<OutboxMessage> pending = outboxRepository.readPending(10);

        for (OutboxMessage msg : pending) {
            try {
                LinkUpdate dto = objectMapper.readValue(msg.getPayload(), LinkUpdate.class);

                LinkUpdateEvent event = LinkUpdateEvent.newBuilder()
                        .setId(dto.id())
                        .setUrl(dto.url().toString())
                        .setDescription(dto.description())
                        .setTgChatIds(dto.tgChatIds())
                        .build();

                kafkaTemplate.send(topicName, event).get();

                outboxRepository.updateStatus(msg.getId(), OutboxStatus.SENT);
                log.info("Successfully processed outbox message: {}", msg.getId());

            } catch (Exception e) {
                log.error("Failed to process outbox message {}: {}", msg.getId(), e.getMessage());
            }
        }
    }
}
