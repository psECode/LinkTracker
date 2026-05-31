package backend.academy.linktracker.scrapper.infrastructure.repository.outboxMessages.jpa;

import backend.academy.linktracker.scrapper.domain.outboxMessages.OutboxStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxJpaRepositoryInterface extends JpaRepository<OutboxJpaEntity, UUID> {
    List<OutboxJpaEntity> findAllByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);
}
