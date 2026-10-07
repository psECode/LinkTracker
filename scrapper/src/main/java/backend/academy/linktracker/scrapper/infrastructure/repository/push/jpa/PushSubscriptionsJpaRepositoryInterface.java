package backend.academy.linktracker.scrapper.infrastructure.repository.push.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushSubscriptionsJpaRepositoryInterface extends JpaRepository<PushSubscriptionJpaEntity, UUID> {

    List<PushSubscriptionJpaEntity> findByUserId(UUID userId);

    Optional<PushSubscriptionJpaEntity> findByEndpoint(String endpoint);

    void deleteByEndpoint(String endpoint);
}
