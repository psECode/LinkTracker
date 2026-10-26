package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.application.push.usecases.SubscribeWebPushUseCase;
import backend.academy.linktracker.scrapper.application.push.usecases.UnsubscribeWebPushUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.PushPublicKeyResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.PushSubscribeRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.PushUnsubscribeRequest;
import backend.academy.linktracker.scrapper.properties.WebPushProperties;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushController {

    private final SubscribeWebPushUseCase subscribeWebPushUseCase;
    private final UnsubscribeWebPushUseCase unsubscribeWebPushUseCase;
    private final WebPushProperties webPushProperties;
    private final MeterRegistry meterRegistry;

    @GetMapping("/key")
    public ResponseEntity<PushPublicKeyResponse> publicKey() {
        return ResponseEntity.ok(new PushPublicKeyResponse(webPushProperties.getPublicKey()));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@AuthenticationPrincipal Jwt jwt, @RequestBody PushSubscribeRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        meterRegistry.counter("api_requests_total", "source", "web").increment();

        if (request.endpoint() == null
                || request.endpoint().isBlank()
                || request.keys() == null
                || request.keys().p256dh() == null
                || request.keys().auth() == null) {
            return ResponseEntity.badRequest().build();
        }

        subscribeWebPushUseCase.execute(
                userId,
                request.endpoint(),
                request.keys().p256dh(),
                request.keys().auth());
        log.info("Registered push subscription for user {}", userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(
            @AuthenticationPrincipal Jwt jwt, @RequestBody PushUnsubscribeRequest request) {
        meterRegistry.counter("api_requests_total", "source", "web").increment();

        if (request.endpoint() == null || request.endpoint().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        unsubscribeWebPushUseCase.execute(request.endpoint());
        log.info("Deleted push subscription for user {}", UUID.fromString(jwt.getSubject()));
        return ResponseEntity.ok().build();
    }
}
