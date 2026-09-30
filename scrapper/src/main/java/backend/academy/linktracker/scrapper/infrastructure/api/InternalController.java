package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserService;
import backend.academy.linktracker.scrapper.application.users.usecases.RegisterTelegramUserUseCase;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.AddLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.ListLinksResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.UserNotFoundException;
import backend.academy.linktracker.scrapper.infrastructure.api.mappers.SubscriptionToLinkResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.GetUsersSubscriptionsUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.SubscribeUserUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.UnregisterUserUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.UnsubscribeUserUseCase;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final RegisterTelegramUserUseCase registerTelegramUserUseCase;
    private final UnregisterUserUseCase unregisterUserUseCase;
    private final GetUsersSubscriptionsUseCase getUsersSubscriptionsUseCase;
    private final SubscribeUserUseCase subscribeUserUseCase;
    private final UnsubscribeUserUseCase unsubscribeUserUseCase;
    private final ReadUserService readUserService;
    private final SubscriptionToLinkResponse responseMapper;

    private final MeterRegistry meterRegistry;

    @PostMapping("/tg-chat/{id}")
    public ResponseEntity<Void> register(@PathVariable Long id) {
        meterRegistry.counter("api_requests_total", "source", "bot").increment();
        registerTelegramUserUseCase.execute(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tg-chat/{id}")
    public ResponseEntity<Void> unregister(@PathVariable Long id) {
        meterRegistry.counter("api_requests_total", "source", "bot").increment();
        readUserService
                .readByTelegramId(id)
                .ifPresentOrElse(user -> unregisterUserUseCase.execute(user.getId()), () -> {
                    throw new UserNotFoundException(id);
                });
        return ResponseEntity.ok().build();
    }

    @GetMapping("/links")
    public ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        meterRegistry.counter("api_requests_total", "source", "bot").increment();
        UUID userId = resolveUserId(chatId);

        List<SubscriptionResult> results = getUsersSubscriptionsUseCase.execute(userId);
        List<LinkResponse> responses = results.stream()
                .map(r -> responseMapper.map(r.subscription(), r.link(), r.user()))
                .toList();

        return ResponseEntity.ok(new ListLinksResponse(responses, responses.size()));
    }

    @PostMapping("/links")
    public ResponseEntity<LinkResponse> addLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request) {
        meterRegistry.counter("api_requests_total", "source", "bot").increment();
        UUID userId = resolveUserId(chatId);

        SubscriptionResult res = subscribeUserUseCase.execute(userId, request.link(), request.tags());
        return ResponseEntity.ok(responseMapper.map(res.subscription(), res.link(), res.user()));
    }

    @DeleteMapping("/links")
    public ResponseEntity<LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request) {
        meterRegistry.counter("api_requests_total", "source", "bot").increment();
        UUID userId = resolveUserId(chatId);

        SubscriptionResult res =
                unsubscribeUserUseCase.execute(userId, request.link().toString());
        return ResponseEntity.ok(responseMapper.map(res.subscription(), res.link(), res.user()));
    }

    private UUID resolveUserId(Long chatId) {
        return readUserService
                .readByTelegramId(chatId)
                .map(User::getId)
                .orElseThrow(() -> new UserNotFoundException(chatId));
    }
}
