package backend.academy.linktracker.scrapper.infrastructure.repository.links.jpa;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface LinkJpaRepositoryInterface extends JpaRepository<LinkJpaEntity, UUID> {
    Optional<LinkJpaEntity> findByUrl(String url);

    List<LinkJpaEntity> findAllByNextCheckAtBefore(OffsetDateTime time);

    List<LinkJpaEntity> findAllByIdIn(Set<UUID> ids);

    List<LinkJpaEntity> findAllByNextCheckAtBeforeOrderByNextCheckAtAsc(OffsetDateTime now, Pageable pageable);
}
