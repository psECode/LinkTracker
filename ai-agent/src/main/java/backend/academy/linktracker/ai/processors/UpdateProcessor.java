package backend.academy.linktracker.ai.processors;

import com.example.notification.RawUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateProcessor {

    private final MessageProcessor messageProcessor;
    private final GroupingService groupingService;

    @KafkaListener(topics = "${app.kafka.raw-updates-topic}")
    public void onRawUpdate(RawUpdateEvent event) {
        log.info("Received raw update id={}", event.getId());

        messageProcessor.process(event).ifPresent(groupingService::add);
    }
}
