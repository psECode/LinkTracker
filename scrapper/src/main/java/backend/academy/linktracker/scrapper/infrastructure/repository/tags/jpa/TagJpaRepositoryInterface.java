package backend.academy.linktracker.scrapper.infrastructure.repository.tags.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagJpaRepositoryInterface extends JpaRepository<TagJpaEntity, TagJpaEntity.TagId> {

    List<TagJpaEntity> findAllById_SubscriptionId(UUID subscriptionId);
}
