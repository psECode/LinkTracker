package backend.academy.linktracker.scrapper.infrastructure.push;

import backend.academy.linktracker.logging.LogArgs;
import backend.academy.linktracker.scrapper.domain.push.PushSubscription;
import backend.academy.linktracker.scrapper.domain.push.PushSubscriptionRepository;
import backend.academy.linktracker.scrapper.properties.WebPushProperties;
import java.net.http.HttpClient;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class WebPushSender {

    private static final int TTL_SECONDS = 24 * 3600;

    private final WebPushProperties properties;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final RestClient restClient;

    public WebPushSender(WebPushProperties properties, PushSubscriptionRepository pushSubscriptionRepository) {
        this.properties = properties;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        HttpClient httpClient =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public boolean enabled() {
        return properties.configured();
    }

    public void send(PushSubscription subscription, String message) {
        if (!enabled()) {
            return;
        }
        try {
            byte[] body = WebPushCrypto.encrypt(subscription.p256dh(), subscription.auth(), message);
            String authorization = WebPushCrypto.vapidAuthorizationHeader(
                    properties.getPrivateKey(),
                    properties.getPublicKey(),
                    subscription.endpoint(),
                    properties.getSubject());

            ResponseEntity<Void> response = restClient
                    .post()
                    .uri(subscription.endpoint())
                    .headers(headers -> applyHeaders(headers, authorization))
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.debug("Web Push delivered", LogArgs.of("endpoint", subscription.endpoint(), "status", response.getStatusCode()));
       } catch (org.springframework.web.client.HttpClientErrorException.NotFound
                | org.springframework.web.client.HttpClientErrorException.Gone e) {
            log.info("Web Push subscription is invalid, deleting", LogArgs.of("status", e.getStatusCode(), "endpoint", subscription.endpoint()));
           pushSubscriptionRepository.deleteByEndpoint(subscription.endpoint());
        } catch (Exception e) {
            log.warn("Failed to deliver Web Push", LogArgs.of("endpoint", subscription.endpoint(), "error", e.getMessage()), e);
       }
    }

    private void applyHeaders(HttpHeaders headers, String authorization) {
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set("TTL", String.valueOf(TTL_SECONDS));
       headers.set("Urgency", "normal");
       headers.set("Content-Encoding", "aes128gcm");
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
    }
}
