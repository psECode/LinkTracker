package backend.academy.linktracker.scrapper.application.subscriptions.usecases;

import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadUsersUuidsByLinkIdUseCase {
    private final ReadSubscriptionService readSubscriptionService;

    public List<UUID> execute(UUID linkId) {
        List<Subscription> subscriptions = readSubscriptionService.readByLinkUUID(linkId);
        return subscriptions.stream().map(Subscription::getUserId).toList();
    }
}
