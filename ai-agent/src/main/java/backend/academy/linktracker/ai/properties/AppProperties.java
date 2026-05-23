package backend.academy.linktracker.ai.properties;

import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private Filtering filtering;

    @NotNull
    private Summarization summarization;

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
}
