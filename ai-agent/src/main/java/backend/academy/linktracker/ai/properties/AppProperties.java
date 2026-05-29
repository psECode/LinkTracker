package backend.academy.linktracker.ai.properties;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "ai-agent")
@Validated
@Getter
@Setter
public class AppProperties {
    private Filtering filtering;
    private Summarization summarization;
    private Prioritization prioritization;
    private Grouping grouping;
    private String processorType;

    @Getter
    @Setter
    public static class Filtering {
        private List<String> stopWords;
        private List<String> excludedAuthors;
        private int minLength;
    }

    @Getter
    @Setter
    public static class Summarization {
        private int threshold;
    }

    @Getter
    @Setter
    public static class Prioritization {
        private List<String> highKeywords;
        private List<String> lowKeywords;
    }

    @Getter
    @Setter
    public static class Grouping {
        private long windowMs;
    }
}
