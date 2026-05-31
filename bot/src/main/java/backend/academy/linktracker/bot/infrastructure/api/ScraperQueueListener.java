package backend.academy.linktracker.bot.infrastructure.api;

import backend.academy.linktracker.bot.application.bot.usecases.ProcessUpdateUseCase;
import backend.academy.linktracker.bot.domain.api.dtos.LinkUpdate;
import com.example.notification.LinkUpdateEvent;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScraperQueueListener {

    private final ProcessUpdateUseCase processUpdateUseCase;

    @KafkaListener(
            topics = "${app.kafka.topic-name}",
            groupId = "bot-group",
            containerFactory = "kafkaListenerContainerFactory")
    public void listen(LinkUpdateEvent event) {
        log.info("got message from kafka: {}", event.getUrl());

        try {
            LinkUpdate update = new LinkUpdate(
                    event.getId(),
                    URI.create(event.getUrl().toString()),
                    event.getDescription().toString(),
                    event.getTgChatIds());

            log.debug("data: {}", update);

            processUpdateUseCase.execute(update);

            log.info("success for ID {} ", update.id());

        } catch (Exception e) {
            log.error("exception: {}. retry.", e.getMessage());
            throw e;
        }
    }
}
