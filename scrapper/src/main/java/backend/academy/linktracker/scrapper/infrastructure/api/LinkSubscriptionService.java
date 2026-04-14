package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.application.links.usecases.CreateTrackedLinkUseCase;
import backend.academy.linktracker.scrapper.application.links.usecases.ReadLinksByUuidsUseCase;
import backend.academy.linktracker.scrapper.application.links.usecases.ReadTrackedLinkByUrlUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.CreateSubscriptionUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.DeleteSubscriptionUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadAllUsersSubscriptionsUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadSubscriptionUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserByTgIdUseCase;
import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.dtos.CreateTrackedLinkDTO;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.CreateSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.ReadSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.InvalidLinkException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.LinkNotFoundException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.SubscriptionNotFoundException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.UserNotFoundException;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkSubscriptionService {

    private final ReadUserByTgIdUseCase readUserUseCase;
    private final CreateTrackedLinkUseCase createTrackedLinkUseCase;
    private final ReadTrackedLinkByUrlUseCase readLinkUseCase;
    private final CreateSubscriptionUseCase createSubscriptionUseCase;
    private final DeleteSubscriptionUseCase deleteSubscriptionUseCase;
    private final ReadSubscriptionUseCase readSubscriptionUseCase;
    private final SchedulerProperties properties;
    private final ReadAllUsersSubscriptionsUseCase readAllUsersSubscriptionsUseCase;
    private final ReadLinksByUuidsUseCase readLinksByUuidsUseCase;

    @Transactional
    public SubscriptionResult subscribe(Long chatId, URI url, List<String> tags) {
        User user = readUserUseCase.execute(chatId).orElseThrow(() -> new UserNotFoundException(chatId));

        LinkType type =
                LinkType.of(url.toString()).orElseThrow(() -> new InvalidLinkException("Сервис не поддерживается"));

        OffsetDateTime now = OffsetDateTime.now();

        CreateTrackedLinkDTO dto = new CreateTrackedLinkDTO(
                url.toString(), now.plus(properties.getInterval()), type, now, properties.getLinkCheckInterval());
        Link link = createTrackedLinkUseCase
                .execute(dto)
                .orElseThrow(() -> new RuntimeException("Ошибка при обработке ссылки"));

        ReadSubscriptionDTO readDto = new ReadSubscriptionDTO(user.getId(), link.getId());
        if (readSubscriptionUseCase.execute(readDto).isPresent()) {
            throw new LinkAlreadyTrackedException("Вы уже подписаны на эту ссылку");
        }

        Subscription sub = createSubscriptionUseCase
                .execute(new CreateSubscriptionDTO(user.getId(), link.getId(), tags))
                .orElseThrow(() -> new RuntimeException("Ошибка сохранения подписки"));

        return new SubscriptionResult(sub, link, user);
    }

    @Transactional
    public SubscriptionResult unsubscribe(Long chatId, String url) {
        User user = readUserUseCase.execute(chatId).orElseThrow(() -> new UserNotFoundException(chatId));

        Link link = readLinkUseCase.execute(url).orElseThrow(() -> new LinkNotFoundException(url));

        var readDto = new ReadSubscriptionDTO(user.getId(), link.getId());
        Subscription sub = readSubscriptionUseCase
                .execute(readDto)
                .orElseThrow(() -> new SubscriptionNotFoundException(chatId, url));

        deleteSubscriptionUseCase.execute(sub.getId());

        return new SubscriptionResult(sub, link, user);
    }

    @Transactional
    public List<SubscriptionResult> getAllSubscriptions(Long chatId) {
        User user = readUserUseCase.execute(chatId).orElseThrow(() -> new UserNotFoundException(chatId));

        List<Subscription> subs = readAllUsersSubscriptionsUseCase.execute(user.getId());
        if (subs.isEmpty()) return List.of();

        Set<UUID> linkIds = subs.stream().map(Subscription::getLinkId).collect(Collectors.toSet());
        Map<UUID, Link> linksMap =
                readLinksByUuidsUseCase.execute(linkIds).stream().collect(Collectors.toMap(Link::getId, l -> l));

        return subs.stream()
                .map(sub -> new SubscriptionResult(sub, linksMap.get(sub.getLinkId()), user))
                .toList();
    }
}
