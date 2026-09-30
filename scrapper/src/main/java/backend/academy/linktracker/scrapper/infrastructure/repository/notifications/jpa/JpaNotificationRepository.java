package backend.academy.linktracker.scrapper.infrastructure.repository.notifications.jpa;

import backend.academy.linktracker.scrapper.domain.notifications.NotificationRepository;
import backend.academy.linktracker.scrapper.domain.notifications.dtos.CreateNotificationDTO;
import backend.academy.linktracker.scrapper.domain.notifications.entities.Notification;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jpa")
public class JpaNotificationRepository implements NotificationRepository {
    private final NotificationJpaRepositoryInterface jpa;

    private Notification toDomain(NotificationJpaEntity e) {
        return new Notification(
                e.getId(), e.getUserId(), e.getLinkId(), e.getMessage(), e.getCreatedAt(), e.getReadAt());
    }

    @Override
    @Transactional
    public void saveAll(List<CreateNotificationDTO> dtos) {
        List<NotificationJpaEntity> entities = dtos.stream()
                .map(dto -> new NotificationJpaEntity(
                        UUID.randomUUID(), dto.userId(), dto.linkId(), dto.message(), OffsetDateTime.now(), null))
                .toList();
        jpa.saveAll(entities);
    }

    @Override
    public List<Notification> readByUser(UUID userId, boolean onlyUnread, int limit) {
        Pageable page = PageRequest.of(0, limit);
        var entities = onlyUnread
                ? jpa.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId, page)
                : jpa.findByUserIdOrderByCreatedAtDesc(userId, page);
        return entities.stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public int markRead(UUID userId, List<UUID> ids) {
        List<NotificationJpaEntity> unread = jpa.findByUserIdAndIdInAndReadAtIsNull(userId, ids);
        OffsetDateTime now = OffsetDateTime.now();
        unread.forEach(e -> e.setReadAt(now));
        jpa.saveAll(unread);
        return unread.size();
    }

    @Override
    @Transactional
    public int markAllRead(UUID userId) {
        List<NotificationJpaEntity> unread = jpa.findByUserIdAndReadAtIsNull(userId);
        OffsetDateTime now = OffsetDateTime.now();
        unread.forEach(e -> e.setReadAt(now));
        jpa.saveAll(unread);
        return unread.size();
    }

    @Override
    public long countUnread(UUID userId) {
        return jpa.countByUserIdAndReadAtIsNull(userId);
    }
}
