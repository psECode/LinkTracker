package backend.academy.linktracker.scrapper.infrastructure.repository.subscriptions.jpa;

import backend.academy.linktracker.scrapper.domain.subscriptions.SubscriptionRepository;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.CreateSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.ReadSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jpa")
public class JpaSubscriptionsRepository implements SubscriptionRepository {
    private final SubscriptionsJpaRepositoryInterface jpa;
    private final SubscriptionMapper mapper;

    @Override
    public Optional<Subscription> read(ReadSubscriptionDTO dto) {
        return jpa.findByUserIdAndLinkId(dto.userId(), dto.linkId()).map(mapper::toDomain);
    }

    @Override
    public List<Subscription> readByUser(UUID userId) {
        return jpa.findAllByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Subscription> readByLink(UUID linkId) {
        return jpa.findAllByLinkId(linkId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Optional<Subscription> save(CreateSubscriptionDTO dto) {
        SubscriptionJpaEntity entity =
                new SubscriptionJpaEntity(UUID.randomUUID(), dto.userId(), dto.linkId(), dto.tags());
        return Optional.of(mapper.toDomain(jpa.save(entity)));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        jpa.deleteById(id);
    }
}
