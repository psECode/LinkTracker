package backend.academy.linktracker.scrapper;

import backend.academy.linktracker.scrapper.infrastructure.api.OutboxProcessor;
import com.example.notification.LinkUpdateEvent;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(
        properties = {"app.access-type=jdbc", "spring.jpa.hibernate.ddl-auto=none", "spring.liquibase.enabled=false"})
@EnableAutoConfiguration(
        excludeName = {
            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
            "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
        })
class JdbcSubscriptionTest {
    @MockitoBean
    private KafkaTemplate<String, LinkUpdateEvent> kafkaTemplate;

    @MockitoBean
    private OutboxProcessor outboxProcessor;
}
