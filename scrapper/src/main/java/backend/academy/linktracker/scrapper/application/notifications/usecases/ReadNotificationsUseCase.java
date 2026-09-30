package backend.academy.linktracker.scrapper.application.notifications.usecases;

import backend.academy.linktracker.scrapper.domain.notifications.NotificationRepository;
import backend.academy.linktracker.scrapper.domain.notifications.dtos.NotificationsPage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public NotificationsPage execute(UUID userId, boolean onlyUnread, int limit) {
        int cappedLimit = Math.min(Math.max(limit, 1), 100);
        return new NotificationsPage(
                notificationRepository.readByUser(userId, onlyUnread, cappedLimit),
                notificationRepository.countUnread(userId));
    }
}
