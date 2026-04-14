package backend.academy.linktracker.scrapper.infrastructure.api.usecases;

import backend.academy.linktracker.scrapper.application.links.usecases.ReadTrackedLinkService;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadSubscriptionService;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserService;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.SubscriptionResult;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetUsersSubscriptionsUseCase {

    private final ReadUserService readUserService;
    private final ReadSubscriptionService readSubscriptionService;
    private final ReadTrackedLinkService readTrackedLinkService;

    @Transactional
    public List<SubscriptionResult> execute(Long chatId) {
        User user = readUserService.readByChatId(chatId)
            .orElseThrow(() -> new UserNotFoundException(chatId));

        List<Subscription> subs = readSubscriptionService.readByUserUUID(user.getId());
        if (subs.isEmpty()) {
            return List.of();
        }

        Set<UUID> linkIds = subs.stream()
            .map(Subscription::getLinkId)
            .collect(Collectors.toSet());

        Map<UUID, Link> linksMap = readTrackedLinkService.readByUUIDs(linkIds)
            .stream()
            .collect(Collectors.toMap(Link::getId, l -> l));

        return subs.stream()
            .map(sub -> new SubscriptionResult(sub, linksMap.get(sub.getLinkId()), user))
            .toList();
    }
}
