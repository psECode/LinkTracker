package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.application.links.usecases.CreateTrackedLinkUseCase;
import backend.academy.linktracker.scrapper.application.links.usecases.ReadLinksByUuidsUseCase;
import backend.academy.linktracker.scrapper.application.links.usecases.ReadTrackedLinkByUrlUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.CreateSubscriptionUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.DeleteSubscriptionUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadAllUsersSubscriptionsUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadSubscriptionUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.CreateUserUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.DeleteUserUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserByTgIdUseCase;
import backend.academy.linktracker.scrapper.domain.api.dtos.AddLinkRequest;
import backend.academy.linktracker.scrapper.domain.api.dtos.LinkResponse;
import backend.academy.linktracker.scrapper.domain.api.dtos.ListLinksResponse;
import backend.academy.linktracker.scrapper.domain.api.dtos.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.dtos.CreateTrackedLinkDTO;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.CreateSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.ReadSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import backend.academy.linktracker.scrapper.domain.users.dtos.CreateUserDto;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.InvalidLinkException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.LinkNotFoundException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.SubscriptionNotFoundException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.UserNotFoundException;
import backend.academy.linktracker.scrapper.infrastructure.api.mappers.SubscriptionToLinkResponse;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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

    private final CreateUserUseCase createUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final CreateTrackedLinkUseCase createTrackedLinkUseCase;
    private final ReadUserByTgIdUseCase readUserByTgIdUseCase;
    private final ReadAllUsersSubscriptionsUseCase readAllUsersSubscriptionsUseCase;
    private final ReadLinksByUuidsUseCase readLinksByUuidsUseCase;
    private final SubscriptionToLinkResponse subscriptionToLinkResponse;
    private final ReadTrackedLinkByUrlUseCase readTrackedLinkByUrlUseCase;
    private final ReadSubscriptionUseCase readSubscriptionUseCase;
    private final DeleteSubscriptionUseCase deleteSubscriptionUseCase;
    private final CreateSubscriptionUseCase createSubscriptionUseCase;
    private final SchedulerProperties properties;

    @PostMapping("/tg-chat/{id}")
    public ResponseEntity<Void> registerUser(@PathVariable Long id) {
        CreateUserDto dto = CreateUserDto.builder().chatId(id).build();
        createUserUseCase.execute(dto);
        log.info("Добавляем юзера {}", sanitize(id));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tg-chat/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = readUserByTgIdUseCase.execute(id).orElseThrow(() -> new UserNotFoundException(id));
        deleteUserUseCase.execute(user.getId());
        log.info("Удаляем юзера {}", sanitize(id));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/links")
    public ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        User user = readUserByTgIdUseCase.execute(chatId).orElseThrow(() -> new UserNotFoundException(chatId));

        List<Subscription> subs = readAllUsersSubscriptionsUseCase.execute(user.getId());

        if (subs.isEmpty()) {
            return ResponseEntity.ok(new ListLinksResponse(List.of(), 0));
        }

        Set<UUID> linkIds = subs.stream().map(Subscription::getLinkId).collect(Collectors.toSet());

        List<Link> links = readLinksByUuidsUseCase.execute(linkIds);

        Map<UUID, Link> linksMap = links.stream().collect(Collectors.toMap(Link::getId, link -> link));

        List<LinkResponse> responses = subs.stream()
                .map(sub -> subscriptionToLinkResponse.map(sub, linksMap.get(sub.getLinkId()), user))
                .toList();

        return ResponseEntity.ok(new ListLinksResponse(responses, responses.size()));
    }

    @PostMapping("/links")
    public ResponseEntity<LinkResponse> addLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request) {

        User user = readUserByTgIdUseCase.execute(chatId).orElseThrow(() -> new UserNotFoundException(chatId));

        OffsetDateTime now = OffsetDateTime.now();

        LinkType type = LinkType.of(String.valueOf(request.link()))
                .orElseThrow(
                        () -> new InvalidLinkException("Такой сервис еще не поддерживается, либо ссылка неправильная"));

        CreateTrackedLinkDTO linkDto = new CreateTrackedLinkDTO(
                request.link().toString(),
                now.plus(properties.getInterval()),
                type,
                now,
                properties.getLinkCheckInterval());

        Link link = createTrackedLinkUseCase
                .execute(linkDto)
                .orElseThrow(() -> new RuntimeException("Не удалось сохранить ссылку"));

        ReadSubscriptionDTO subscriptionDTO = new ReadSubscriptionDTO(user.getId(), link.getId());

        if (readSubscriptionUseCase.execute(subscriptionDTO).isPresent()) {
            throw new LinkAlreadyTrackedException("Ссылка уже отслеживается");
        }

        CreateSubscriptionDTO subscriptionDto = new CreateSubscriptionDTO(user.getId(), link.getId(), request.tags());

        Subscription sub = createSubscriptionUseCase
                .execute(subscriptionDto)
                .orElseThrow(() -> new RuntimeException("Не удалось создать подписку"));

        log.info("создал подписку для юзера: {} для ссылки {}", sub.getUserId(), sub.getLinkId());

        return ResponseEntity.ok(subscriptionToLinkResponse.map(sub, link, user));
    }

    @DeleteMapping("/links")
    public ResponseEntity<LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request) {

        User user = readUserByTgIdUseCase.execute(chatId).orElseThrow(() -> new UserNotFoundException(chatId));

        Link link = readTrackedLinkByUrlUseCase
                .execute(request.link().toString())
                .orElseThrow(() -> new LinkNotFoundException(request.link().toString()));

        System.out.println("found link");

        ReadSubscriptionDTO subscriptionDTO = new ReadSubscriptionDTO(user.getId(), link.getId());

        Subscription sub = readSubscriptionUseCase
                .execute(subscriptionDTO)
                .orElseThrow(() -> new SubscriptionNotFoundException(user.getChatId(), link.getUrl()));

        System.out.println("found subscription");

        deleteSubscriptionUseCase.execute(sub.getId());

        return ResponseEntity.ok(subscriptionToLinkResponse.map(sub, link, user));
    }

    private String sanitize(Object input) {
        return String.valueOf(input).replaceAll("[\r\n]", "");
    }
}
