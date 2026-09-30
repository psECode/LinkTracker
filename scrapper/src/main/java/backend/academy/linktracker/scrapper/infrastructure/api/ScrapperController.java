package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.infrastructure.api.dtos.AddLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.ListLinksResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.mappers.SubscriptionToLinkResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.GetUsersSubscriptionsUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.SubscribeUserUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.UnsubscribeUserUseCase;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScrapperController {

    private final GetUsersSubscriptionsUseCase getUsersSubscriptionsUseCase;
    private final SubscribeUserUseCase subscribeUserUseCase;
    private final UnsubscribeUserUseCase unsubscribeUserUseCase;
    private final SubscriptionToLinkResponse responseMapper;

    private final MeterRegistry meterRegistry;

    @GetMapping("/links")
    public ResponseEntity<ListLinksResponse> getLinks(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        meterRegistry.counter("api_requests_total", "source", "web").increment();

        List<SubscriptionResult> results = getUsersSubscriptionsUseCase.execute(userId);
        List<LinkResponse> responses = results.stream()
                .map(r -> responseMapper.map(r.subscription(), r.link(), r.user()))
                .toList();

        return ResponseEntity.ok(new ListLinksResponse(responses, responses.size()));
    }

    @PostMapping("/links")
    public ResponseEntity<LinkResponse> addLink(@AuthenticationPrincipal Jwt jwt, @RequestBody AddLinkRequest request) {
        UUID userId = extractUserId(jwt);
        meterRegistry.counter("api_requests_total", "source", "web").increment();

        SubscriptionResult res = subscribeUserUseCase.execute(userId, request.link(), request.tags());
        return ResponseEntity.ok(responseMapper.map(res.subscription(), res.link(), res.user()));
    }

    @DeleteMapping("/links")
    public ResponseEntity<LinkResponse> removeLink(
            @AuthenticationPrincipal Jwt jwt, @RequestBody RemoveLinkRequest request) {
        UUID userId = extractUserId(jwt);
        meterRegistry.counter("api_requests_total", "source", "web").increment();

        SubscriptionResult res =
                unsubscribeUserUseCase.execute(userId, request.link().toString());
        return ResponseEntity.ok(responseMapper.map(res.subscription(), res.link(), res.user()));
    }

    private UUID extractUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
