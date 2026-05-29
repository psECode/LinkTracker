package backend.academy.linktracker.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.processors.GroupingService;
import backend.academy.linktracker.ai.properties.AppProperties;
import backend.academy.linktracker.ai.properties.KafkaProperties;
import com.example.notification.ProcessedUpdateEvent;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class GroupingTest {

    @Mock
    private KafkaTemplate<String, ProcessedUpdateEvent> kafkaTemplate;

    @Mock
    private KafkaProperties kafkaProperties;

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private GroupingService groupingService;

    @Test
    void groupingMultipleUpdates() {
        when(kafkaProperties.getProcessedUpdatesTopic()).thenReturn("output-topic");
        List<Long> chats = List.of(123L);

        groupingService.add(createProcessed(1, "Msg 1", "LOW", chats));
        groupingService.add(createProcessed(2, "Msg 2", "HIGH", chats));

        groupingService.flush();

        ArgumentCaptor<ProcessedUpdateEvent> captor = ArgumentCaptor.forClass(ProcessedUpdateEvent.class);
        verify(kafkaTemplate).send(eq("output-topic"), captor.capture());

        ProcessedUpdateEvent result = captor.getValue();
        assertThat(result.getDescription().toString()).contains("1. Msg 1", "2. Msg 2");
        assertThat(result.getPriority().toString()).hasToString("HIGH");
    }

    private ProcessedUpdateEvent createProcessed(long id, String desc, String prio, List<Long> chats) {
        return ProcessedUpdateEvent.newBuilder()
                .setId(id)
                .setDescription(desc)
                .setPriority(prio)
                .setTgChatIds(chats)
                .build();
    }
}
