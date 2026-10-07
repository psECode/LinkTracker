package backend.academy.linktracker.scrapper.infrastructure.repository.push.jpa;

import backend.academy.linktracker.scrapper.domain.push.PushSubscription;
import backend.academy.linktracker.scrapper.domain.push.PushSubscriptionRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jpa")
public class JpaPushSubscriptionRepository implements PushSubscriptionRepository {
    private final PushSubscriptionsJpaRepositoryInterface jpa;

    private PushSubscription toDomain(PushSubscriptionJpaEntity e) {
        return new PushSubscription(e.getId(), e.getUserId(), e.getEndpoint(), e.getP256dh(), e.getAuth());
    }

    @Override
    @Transactional
    public void save(PushSubscription subscription) {
        Optional<PushSubscriptionJpaEntity> existing = jpa.findByEndpoint(subscription.endpoint());
        PushSubscriptionJpaEntity entity = existing.orElseGet(() -> new PushSubscriptionJpaEntity(
                subscription.id() != null ? subscription.id() : UUID.randomUUID(),
                subscription.userId(),
                subscription.endpoint(),
                subscription.p256dh(),
                subscription.auth(),
                OffsetDateTime.now()));
        entity.setUserId(subscription.userId());
        entity.setP256dh(subscription.p256dh());
        entity.setAuth(subscription.auth());
        jpa.save(entity);
    }

    @Override
    public List<PushSubscription> findByUser(UUID userId) {
        return jpa.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteByEndpoint(String endpoint) {
        jpa.deleteByEndpoint(endpoint);
    }
}
