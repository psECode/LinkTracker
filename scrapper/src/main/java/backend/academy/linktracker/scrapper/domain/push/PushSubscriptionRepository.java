package backend.academy.linktracker.scrapper.domain.push;

import java.util.List;
import java.util.UUID;

public interface PushSubscriptionRepository {

    void save(PushSubscription subscription);

    List<PushSubscription> findByUser(UUID userId);

    void deleteByEndpoint(String endpoint);
}
