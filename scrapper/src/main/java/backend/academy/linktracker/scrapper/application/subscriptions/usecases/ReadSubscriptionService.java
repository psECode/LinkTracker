package backend.academy.linktracker.scrapper.application.subscriptions.usecases;

import backend.academy.linktracker.scrapper.domain.subscriptions.SubscriptionRepository;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.ReadSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReadSubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    public Optional<Subscription> read(ReadSubscriptionDTO dto) {
        return subscriptionRepository.read(dto);
    }

    public List<Subscription> readByUserUUID(UUID chatId) {
        return subscriptionRepository.readByUser(chatId);
    }

    public List<Subscription> readByLinkUUID(UUID linkId) {
        return subscriptionRepository.readByLink(linkId);
    }
}
