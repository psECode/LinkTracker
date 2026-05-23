package backend.academy.linktracker.scrapper.infrastructure.repository.subscriptions.jpa;

import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMapper {
    public Subscription toDomain(SubscriptionJpaEntity e) {
        return Subscription.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .linkId(e.getLinkId())
                .tags(e.getTags())
                .build();
    }

    public SubscriptionJpaEntity toEntity(Subscription d) {
        return new SubscriptionJpaEntity(d.getId(), d.getUserId(), d.getLinkId(), d.getTags());
    }
}
