package backend.academy.linktracker.bot.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.lessThan;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.linktracker.bot.infrastructure.bot.TelegramMessageSender;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class TelegramCBTest {

    @Autowired
    private TelegramMessageSender telegramMessageSender;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private CircuitBreakerRegistry registry;

    @Value("${app.telegram.token}")
    private String token;

    @BeforeEach
    void setUp() {
        registry.circuitBreaker("telegramCB").reset();
        wireMockServer.resetAll();
    }

    @Test
    void CBOpen() {
        // g
        String path = "/bot" + token + "/sendMessage";
        wireMockServer.stubFor(post(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"ok\":false,\"error_code\":500}")));

        // w
        for (int i = 0; i < 10; i++) {
            try {
                telegramMessageSender.sendText(1L, "test");
            } catch (Exception ignored) {
            }
        }

        // t
        CircuitBreaker cb = registry.circuitBreaker("telegramCB");
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThrows(CallNotPermittedException.class, () -> telegramMessageSender.sendText(1L, "test"));
        wireMockServer.verify(lessThan(10), postRequestedFor(urlEqualTo(path)));
    }
}
