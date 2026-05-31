package backend.academy.linktracker.scrapper.infrastructure.repository.outboxMessages.jpa;

import backend.academy.linktracker.scrapper.domain.outboxMessages.OutboxMessage;
import backend.academy.linktracker.scrapper.domain.outboxMessages.OutboxRepository;
import backend.academy.linktracker.scrapper.domain.outboxMessages.OutboxStatus;
import backend.academy.linktracker.scrapper.domain.outboxMessages.dtos.CreateOutboxMessageDTO;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jpa")
public class JpaOutboxRepository implements OutboxRepository {
    private final OutboxJpaRepositoryInterface jpa;

    private OutboxMessage toDomain(OutboxJpaEntity entity) {
        return OutboxMessage.builder()
                .id(entity.getId())
                .payload(entity.getPayload())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public OutboxMessage save(CreateOutboxMessageDTO dto) {
        OutboxJpaEntity entity =
                new OutboxJpaEntity(UUID.randomUUID(), dto.payload(), OutboxStatus.PENDING, OffsetDateTime.now());
        return toDomain(jpa.save(entity));
    }

    @Override
    public List<OutboxMessage> readPending(int limit) {
        return jpa.findAllByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, PageRequest.of(0, limit)).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void updateStatus(UUID id, OutboxStatus status) {
        jpa.findById(id).ifPresent(entity -> entity.setStatus(status));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        jpa.deleteById(id);
    }
}
