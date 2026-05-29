package backend.academy.linktracker.ai.processors;

import backend.academy.linktracker.ai.properties.KafkaProperties;
import com.example.notification.ProcessedUpdateEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupingService {

    private final KafkaTemplate<String, ProcessedUpdateEvent> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final Map<List<Long>, List<ProcessedUpdateEvent>> buffer = new ConcurrentHashMap<>();

    public void add(ProcessedUpdateEvent event) {
        List<Long> key = new ArrayList<>(event.getTgChatIds());
        buffer.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(event);
    }

    @Scheduled(fixedDelayString = "${ai-agent.grouping.window-ms}")
    public void flush() {
        if (buffer.isEmpty()) return;

        var it = buffer.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            var updates = entry.getValue();
            if (!updates.isEmpty()) {
                var result = (updates.size() == 1) ? updates.getFirst() : merge(entry.getKey(), updates);
                send(result);
            }
            it.remove();
        }
    }

    private void send(ProcessedUpdateEvent event) {
        String topic = kafkaProperties.getProcessedUpdatesTopic();

        kafkaTemplate.send(topic, event);
    }

    private ProcessedUpdateEvent merge(List<Long> chatIds, List<ProcessedUpdateEvent> updates) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < updates.size(); i++) {
            sb.append(i + 1)
                    .append(". ")
                    .append(updates.get(i).getDescription())
                    .append("\n");
        }

        String maxPriority = updates.stream()
                .map(u -> u.getPriority().toString())
                .max(Comparator.comparingInt(this::getPriorityWeight))
                .orElse("MEDIUM");

        return ProcessedUpdateEvent.newBuilder()
                .setId(updates.get(0).getId())
                .setDescription(sb.toString().trim())
                .setTgChatIds(chatIds)
                .setPriority(maxPriority)
                .build();
    }

    private int getPriorityWeight(String p) {
        return switch (p) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }
}
