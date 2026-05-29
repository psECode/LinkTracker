package backend.academy.linktracker.ai.processors;

import backend.academy.linktracker.ai.properties.AppProperties;
import com.example.notification.ProcessedUpdateEvent;
import com.example.notification.RawUpdateEvent;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "ai-agent.processor.type", havingValue = "simple", matchIfMissing = true)
public class SimpleMessageProcessor implements MessageProcessor {

    private final AppProperties properties;

    @Override
    public Optional<ProcessedUpdateEvent> process(RawUpdateEvent raw) {
        if (shouldFilter(raw)) {
            return Optional.empty();
        }

        String description = raw.getDescription().toString();
        int threshold = properties.getSummarization().getThreshold();
        if (description.length() > threshold) {
            description = description.substring(0, threshold) + "...";
        }

        String priority = calculatePriority(description);

        ProcessedUpdateEvent processed = ProcessedUpdateEvent.newBuilder()
                .setId(raw.getId())
                .setDescription(description)
                .setTgChatIds(raw.getTgChatIds())
                .setPriority(priority)
                .build();
        return Optional.of(processed);
    }

    private String calculatePriority(String text) {
        String lowerText = text.toLowerCase();
        var prioritizeCfg = properties.getPrioritization();

        boolean hasHigh =
                prioritizeCfg.getHighKeywords().stream().anyMatch(word -> lowerText.contains(word.toLowerCase()));
        if (hasHigh) return "HIGH";

        boolean hasLow =
                prioritizeCfg.getLowKeywords().stream().anyMatch(word -> lowerText.contains(word.toLowerCase()));
        if (hasLow) return "LOW";

        return "MEDIUM";
    }

    private boolean shouldFilter(RawUpdateEvent raw) {
        String description = raw.getDescription().toString().toLowerCase();
        String author = raw.getAuthor().toString().toLowerCase();
        List<String> stopWords = properties.getFiltering().getStopWords();
        if (stopWords != null) {
            for (String word : stopWords) {
                if (description.contains(word.toLowerCase())) {
                    return true;
                }
            }
        }

        List<String> excludedAuthors = properties.getFiltering().getExcludedAuthors();
        if (excludedAuthors != null && excludedAuthors.stream().anyMatch(a -> a.equalsIgnoreCase(author))) {
            return true;
        }

        int minLength = properties.getFiltering().getMinLength();
        if (description.length() < minLength) {
            return true;
        }

        return false;
    }
}
