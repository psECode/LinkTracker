package backend.academy.linktracker.scrapper.domain.subscriptions;

import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.CreateSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.ReadSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {

    Optional<Subscription> save(CreateSubscriptionDTO dto);

    Optional<Subscription> read(ReadSubscriptionDTO dto);

    List<Subscription> readByUser(UUID chatId);

    List<Subscription> readByLink(UUID linkId);

    void delete(UUID id);
}
