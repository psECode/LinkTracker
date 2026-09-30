package backend.academy.linktracker.scrapper.application.notifications.usecases;

import backend.academy.linktracker.scrapper.domain.notifications.NotificationRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarkNotificationsReadUseCase {

    private final NotificationRepository notificationRepository;

    @Transactional
    public int execute(UUID userId, List<UUID> ids, boolean markAll) {
        if (markAll) {
            return notificationRepository.markAllRead(userId);
        }
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return notificationRepository.markRead(userId, ids);
    }
}
