package backend.academy.linktracker.scrapper.infrastructure.repository.subscriptions.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionsJpaRepositoryInterface extends JpaRepository<SubscriptionJpaEntity, UUID> {
    Optional<SubscriptionJpaEntity> findByUserIdAndLinkId(UUID userId, UUID linkId);

    List<SubscriptionJpaEntity> findAllByUserId(UUID userId);

    List<SubscriptionJpaEntity> findAllByLinkId(UUID linkId);
}
