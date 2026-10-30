package backend.academy.linktracker.bot.infrastructure.api;

import backend.academy.linktracker.logging.LogArgs;
import backend.academy.linktracker.bot.application.bot.usecases.ProcessUpdateUseCase;
import backend.academy.linktracker.bot.domain.api.dtos.LinkUpdate;
import backend.academy.linktracker.logging.MdcTraceId;
import com.example.notification.ProcessedUpdateEvent;
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
   public void listen(ProcessedUpdateEvent event) {
        log.info("got message from kafka");

       MdcTraceId.run("kafka-" + event.getId(), () -> {
           try {
                LinkUpdate update = new LinkUpdate(
                        event.getId(),
                        event.getDescription().toString(),
                        event.getPriority().toString(),
                        event.getTgChatIds());

                log.debug("Update received", LogArgs.of("update_id", update.id(), "description", update.description()));

               processUpdateUseCase.execute(update);

                log.info("Update delivered", LogArgs.of("update_id", update.id()));

           } catch (RuntimeException e) {
                log.error("Update delivery failed, retrying", LogArgs.of("error", e.getMessage()), e);
                throw e;
            }
        });
    }
}
