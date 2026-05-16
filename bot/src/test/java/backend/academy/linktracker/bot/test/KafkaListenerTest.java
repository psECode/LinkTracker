package backend.academy.linktracker.bot.test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.application.bot.usecases.ProcessUpdateUseCase;
import com.example.notification.LinkUpdateEvent;
import com.pengrad.telegrambot.TelegramBot;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Slf4j
@ActiveProfiles("test")
class KafkaListenerTest {

    @MockitoBean
    private TelegramBot telegramBot;

    @Autowired
    private KafkaTemplate<String, LinkUpdateEvent> kafkaTemplate;

    @Value("${app.kafka.topic-name}")
    private String topicName;

    @MockitoBean
    private ProcessUpdateUseCase processUpdateUseCase;

    @Test
    void HappyPathTest() {
        LinkUpdateEvent event = LinkUpdateEvent.newBuilder()
                .setId(1L)
                .setUrl("https://github.com/user/repo")
                .setDescription("test")
                .setTgChatIds(List.of(1473932230L))
                .build();

        log.info("Sending Avro test event to topic {}: {}", topicName, event);

        kafkaTemplate.send(topicName, event);

        verify(processUpdateUseCase, timeout(10000).times(1))
                .execute(argThat(receivedUpdate -> receivedUpdate.id().equals(1L)
                        && receivedUpdate.url().toString().equals("https://github.com/user/repo")
                        && receivedUpdate.description().equals("test")));
    }
}
