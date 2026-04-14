package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.infrastructure.api.dtos.AddLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.ListLinksResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.mappers.SubscriptionToLinkResponse;
import java.util.List;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.GetUsersSubscriptionsUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.RegisterUserUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.SubscribeUserUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.UnregisterUserUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.UnsubscribeUserUseCase;
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
@RequestMapping("/")
@RequiredArgsConstructor
public class ScrapperController {

    private final GetUsersSubscriptionsUseCase getUsersSubscriptionsUseCase;
    private final SubscribeUserUseCase subscribeUserUseCase;
    private final UnsubscribeUserUseCase unsubscribeUserUseCase;

    private final RegisterUserUseCase registerUserUseCase;
    private final UnregisterUserUseCase unregisterUserUseCase;
    private final SubscriptionToLinkResponse responseMapper;

    @PostMapping("/tg-chat/{id}")
    public ResponseEntity<Void> register(@PathVariable Long id) {
        registerUserUseCase.execute(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tg-chat/{id}")
    public ResponseEntity<Void> unregister(@PathVariable Long id) {
        unregisterUserUseCase.execute(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/links")
    public ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        List<SubscriptionResult> results = getUsersSubscriptionsUseCase.execute(chatId);

        List<LinkResponse> responses = results.stream()
                .map(r -> responseMapper.map(r.subscription(), r.link(), r.user()))
                .toList();

        return ResponseEntity.ok(new ListLinksResponse(responses, responses.size()));
    }

    @PostMapping("/links")
    public ResponseEntity<LinkResponse> addLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request) {

        SubscriptionResult res = subscribeUserUseCase.execute(chatId, request.link(), request.tags());
        return ResponseEntity.ok(responseMapper.map(res.subscription(), res.link(), res.user()));
    }

    @DeleteMapping("/links")
    public ResponseEntity<LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request) {

        SubscriptionResult res =
                unsubscribeUserUseCase.execute(chatId, request.link().toString());
        return ResponseEntity.ok(responseMapper.map(res.subscription(), res.link(), res.user()));
    }
}
