package backend.academy.linktracker.scrapper.application.subscriptions.usecases;

import backend.academy.linktracker.scrapper.domain.subscriptions.SubscriptionRepository;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadAllUsersSubscriptionsUseCase {
    private final SubscriptionRepository subscriptionRepository;

    public List<Subscription> execute(UUID chatId) {
        return subscriptionRepository.readByUser(chatId);
    }
}
