package backend.academy.linktracker.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.processors.SimpleMessageProcessor;
import backend.academy.linktracker.ai.properties.AppProperties;
import com.example.notification.ProcessedUpdateEvent;
import com.example.notification.RawUpdateEvent;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SimpleMessageProcessorTest {
    @Mock
    private AppProperties properties;

    @Mock
    private AppProperties.Filtering filtering;

    @Mock
    private AppProperties.Summarization summarization;

    @Mock
    private AppProperties.Prioritization prioritization;

    @InjectMocks
    private SimpleMessageProcessor processor;

    @BeforeEach
    void setUp() {
        when(properties.getFiltering()).thenReturn(filtering);
        when(properties.getSummarization()).thenReturn(summarization);
        when(properties.getPrioritization()).thenReturn(prioritization);

        when(filtering.getStopWords()).thenReturn(List.of("spam"));
        when(filtering.getExcludedAuthors()).thenReturn(List.of());
        when(filtering.getMinLength()).thenReturn(5);
        when(summarization.getThreshold()).thenReturn(100);

        when(prioritization.getHighKeywords()).thenReturn(List.of("critical", "security"));
        when(prioritization.getLowKeywords()).thenReturn(List.of("typo", "docs"));
    }

    @Test
    void shouldReturnHighPriority() {
        RawUpdateEvent raw = createRaw("Security fix for critical bug");
        ProcessedUpdateEvent result = processor.process(raw).get();
        assertThat(result.getPriority().toString()).isEqualTo("HIGH");
    }

    @Test
    void shouldReturnMediumPriority() {
        RawUpdateEvent raw = createRaw("Just a regular update message");
        ProcessedUpdateEvent result = processor.process(raw).get();
        assertThat(result.getPriority().toString()).isEqualTo("MEDIUM");
    }

    @Test
    void shouldReturnLowPriority() {
        RawUpdateEvent raw = createRaw("Fixed minor typo in readme");
        ProcessedUpdateEvent result = processor.process(raw).get();
        assertThat(result.getPriority().toString()).isEqualTo("LOW");
    }

    private RawUpdateEvent createRaw(String text) {
        return RawUpdateEvent.newBuilder()
                .setId(1L)
                .setDescription(text)
                .setAuthor("user")
                .setTgChatIds(List.of(1L))
                .build();
    }
}
