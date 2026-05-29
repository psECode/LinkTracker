package backend.academy.linktracker.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.processors.SimpleMessageProcessor;
import backend.academy.linktracker.ai.properties.AppProperties;
import com.example.notification.ProcessedUpdateEvent;
import com.example.notification.RawUpdateEvent;
import java.util.List;
import java.util.Optional;
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

    @InjectMocks
    private SimpleMessageProcessor processor;

    @BeforeEach
    void setUp() {
        when(properties.getFiltering()).thenReturn(filtering);
        when(properties.getSummarization()).thenReturn(summarization);
        when(filtering.getStopWords()).thenReturn(List.of("spam", "ads"));
        when(filtering.getExcludedAuthors()).thenReturn(List.of("bot-user"));
        when(filtering.getMinLength()).thenReturn(20);
        when(summarization.getThreshold()).thenReturn(500);
    }

    @Test
    void shouldPassFilterWhenMessageValid() {
        RawUpdateEvent raw = RawUpdateEvent.newBuilder()
                .setId(1L)
                .setDescription("description                    ")
                .setAuthor("nick")
                .setTgChatIds(List.of(111L))
                .build();

        Optional<ProcessedUpdateEvent> result = processor.process(raw);
        assertThat(result).isPresent();
        ProcessedUpdateEvent out = result.get();
        assertThat(out.getId()).isEqualTo(1L);
        assertThat(out.getDescription()).isEqualTo("description                    ");
    }

    @Test
    void shouldFilterByStopWord() {
        RawUpdateEvent raw = RawUpdateEvent.newBuilder()
                .setId(2L)
                .setDescription("description                     with spam")
                .setAuthor("nick")
                .setTgChatIds(List.of(111L))
                .build();

        Optional<ProcessedUpdateEvent> result = processor.process(raw);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFilterByExcludedAuthor() {
        RawUpdateEvent raw = RawUpdateEvent.newBuilder()
                .setId(3L)
                .setDescription("description                    ")
                .setAuthor("bot-user")
                .setTgChatIds(List.of(111L))
                .build();

        Optional<ProcessedUpdateEvent> result = processor.process(raw);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFilterByMinLength() {
        RawUpdateEvent raw = RawUpdateEvent.newBuilder()
                .setId(4L)
                .setDescription("des")
                .setAuthor("nick")
                .setTgChatIds(List.of(111L))
                .build();

        Optional<ProcessedUpdateEvent> result = processor.process(raw);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldSummarizeLongText() {
        String longText = "a".repeat(600);
        RawUpdateEvent raw = RawUpdateEvent.newBuilder()
                .setId(5L)
                .setDescription(longText)
                .setAuthor("nick")
                .setTgChatIds(List.of(111L))
                .build();

        Optional<ProcessedUpdateEvent> result = processor.process(raw);
        assertThat(result).isPresent();
        ProcessedUpdateEvent out = result.get();
        assertThat(out.getDescription()).hasSize(503);
        assertThat(out.getDescription()).endsWith("...");
    }

    @Test
    void shouldNotSummarizeShortText() {
        RawUpdateEvent raw = RawUpdateEvent.newBuilder()
                .setId(6L)
                .setDescription("description                    ")
                .setAuthor("nick")
                .setTgChatIds(List.of(111L))
                .build();

        Optional<ProcessedUpdateEvent> result = processor.process(raw);
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("description                    ");
    }
}
