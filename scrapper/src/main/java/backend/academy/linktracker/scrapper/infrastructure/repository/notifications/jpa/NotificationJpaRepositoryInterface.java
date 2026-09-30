package backend.academy.linktracker.scrapper.infrastructure.repository.notifications.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepositoryInterface extends JpaRepository<NotificationJpaEntity, UUID> {

    List<NotificationJpaEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<NotificationJpaEntity> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    List<NotificationJpaEntity> findByUserIdAndReadAtIsNull(UUID userId);

    List<NotificationJpaEntity> findByUserIdAndIdInAndReadAtIsNull(UUID userId, List<UUID> ids);

    long countByUserIdAndReadAtIsNull(UUID userId);
}
