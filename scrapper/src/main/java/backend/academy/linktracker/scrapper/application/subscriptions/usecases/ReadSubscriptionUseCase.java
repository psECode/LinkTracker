package backend.academy.linktracker.scrapper.application.subscriptions.usecases;

import backend.academy.linktracker.scrapper.domain.subscriptions.SubscriptionRepository;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.ReadSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadSubscriptionUseCase {
    private final SubscriptionRepository subscriptionRepository;

    public Optional<Subscription> execute(ReadSubscriptionDTO dto) {
        return subscriptionRepository.read(dto);
    }
}
