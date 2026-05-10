package backend.academy.linktracker.scrapper.application.subscriptions.usecases;

import backend.academy.linktracker.scrapper.domain.subscriptions.SubscriptionRepository;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.CreateSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateSubscriptionUseCase {
    private final SubscriptionRepository subscriptionRepository;

    public Optional<Subscription> execute(CreateSubscriptionDTO dto) {
        return subscriptionRepository.save(dto);
    }
}
