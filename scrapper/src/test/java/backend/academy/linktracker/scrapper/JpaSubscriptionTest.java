package backend.academy.linktracker.scrapper;

import backend.academy.linktracker.scrapper.infrastructure.api.OutboxProcessor;
import com.example.notification.LinkUpdateEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(
        properties = {"app.access-type=jpa", "spring.jpa.hibernate.ddl-auto=update", "spring.liquibase.enabled=false"})
class JpaSubscriptionTest {
    @MockitoBean
    private KafkaTemplate<String, LinkUpdateEvent> kafkaTemplate;

    @MockitoBean
    private OutboxProcessor outboxProcessor;
}
