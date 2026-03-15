package backend.academy.linktracker.scrapper.application.subscriptions.usecases;

import backend.academy.linktracker.scrapper.domain.subscriptions.SubscriptionRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteSubscriptionUseCase {
    private final SubscriptionRepository subscriptionRepository;

    public void execute(UUID id) {
        subscriptionRepository.delete(id);
    }
}
