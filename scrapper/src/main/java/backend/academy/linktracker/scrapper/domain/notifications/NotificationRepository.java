package backend.academy.linktracker.scrapper.domain.notifications;

import backend.academy.linktracker.scrapper.domain.notifications.dtos.CreateNotificationDTO;
import backend.academy.linktracker.scrapper.domain.notifications.entities.Notification;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository {

    void saveAll(List<CreateNotificationDTO> dtos);

    List<Notification> readByUser(UUID userId, boolean onlyUnread, int limit);

    int markRead(UUID userId, List<UUID> ids);

    int markAllRead(UUID userId);

    long countUnread(UUID userId);
}
