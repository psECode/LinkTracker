package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkUpdate;
import backend.academy.linktracker.scrapper.infrastructure.api.updateSenders.LinkUpdateSender;
import com.example.notification.LinkUpdateEvent;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(
        properties = {
            "app.access-type=jpa",
            "spring.jpa.hibernate.ddl-auto=update",
            "spring.liquibase.enabled=false",
            "app.use-queue=false"
        })
@ActiveProfiles("test")
class HttpReliabilityTest {

    @Autowired
    private LinkUpdateSender sender;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockitoBean
    protected KafkaTemplate<String, LinkUpdateEvent> kafkaTemplate;

    @DynamicPropertySource
    static void overrideBotUrl(DynamicPropertyRegistry registry) {
        registry.add("app.bot.url", () -> "http://localhost:" + TestcontainersConfiguration.WIREMOCK.port());
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        circuitBreakerRegistry.circuitBreaker("botUpdateCB").reset();
    }

    @SneakyThrows
    @Test
    void shouldFallbackToKafkaIfHTTPDoNotWork() {
        // g
        wireMockServer.stubFor(
                post(urlEqualTo("/updates")).willReturn(aResponse().withStatus(500)));
        LinkUpdate update = new LinkUpdate(1L, URI.create("http://test.com"), "desc", List.of(1L));

        // w
        sender.send(update);

        // t
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(kafkaTemplate, atLeastOnce()).send(anyString(), any());
        });
    }

    @SneakyThrows
    @Test
    void shouldRetryAndFail() {
        // g
        wireMockServer.stubFor(post(urlEqualTo("/updates"))
                .willReturn(aResponse().withStatus(500).withHeader("Content-Type", "application/json")));
        LinkUpdate update = new LinkUpdate(1L, URI.create("http://test.com"), "desc", List.of(1L));

        // w
        sender.send(update);

        // t
        wireMockServer.verify(moreThanOrExactly(2), postRequestedFor(urlEqualTo("/updates")));
    }
}
