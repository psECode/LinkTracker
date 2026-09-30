package backend.academy.linktracker.scrapper.infrastructure.mocks.notifications;

import backend.academy.linktracker.scrapper.domain.notifications.NotificationRepository;
import backend.academy.linktracker.scrapper.domain.notifications.dtos.CreateNotificationDTO;
import backend.academy.linktracker.scrapper.domain.notifications.entities.Notification;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "mock")
public class MemoryNotificationRepository implements NotificationRepository {

    private final List<Notification> storage = new CopyOnWriteArrayList<>();

    @Override
    public void saveAll(List<CreateNotificationDTO> dtos) {
        dtos.forEach(dto -> storage.add(new Notification(
                UUID.randomUUID(), dto.userId(), dto.linkId(), dto.message(), OffsetDateTime.now(), null)));
    }

    @Override
    public List<Notification> readByUser(UUID userId, boolean onlyUnread, int limit) {
        return storage.stream()
                .filter(n -> n.userId().equals(userId))
                .filter(n -> !onlyUnread || n.readAt() == null)
                .sorted(Comparator.comparing(Notification::createdAt).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public int markRead(UUID userId, List<UUID> ids) {
        int updated = 0;
        for (int i = 0; i < storage.size(); i++) {
            Notification n = storage.get(i);
            if (n.userId().equals(userId) && n.readAt() == null && ids.contains(n.id())) {
                storage.set(i, replaceReadAt(n, OffsetDateTime.now()));
                updated++;
            }
        }
        return updated;
    }

    @Override
    public int markAllRead(UUID userId) {
        int updated = 0;
        for (int i = 0; i < storage.size(); i++) {
            Notification n = storage.get(i);
            if (n.userId().equals(userId) && n.readAt() == null) {
                storage.set(i, replaceReadAt(n, OffsetDateTime.now()));
                updated++;
            }
        }
        return updated;
    }

    @Override
    public long countUnread(UUID userId) {
        return storage.stream()
                .filter(n -> n.userId().equals(userId) && n.readAt() == null)
                .count();
    }

    private Notification replaceReadAt(Notification n, OffsetDateTime readAt) {
        return new Notification(n.id(), n.userId(), n.linkId(), n.message(), n.createdAt(), readAt);
    }
}
