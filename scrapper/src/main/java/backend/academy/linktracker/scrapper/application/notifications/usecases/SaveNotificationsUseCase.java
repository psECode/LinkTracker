package backend.academy.linktracker.scrapper.application.notifications.usecases;

import backend.academy.linktracker.scrapper.application.push.usecases.DeliverWebPushUseCase;
import backend.academy.linktracker.scrapper.domain.notifications.NotificationRepository;
import backend.academy.linktracker.scrapper.domain.notifications.dtos.CreateNotificationDTO;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaveNotificationsUseCase {

    private final NotificationRepository notificationRepository;
    private final DeliverWebPushUseCase deliverWebPushUseCase;

    @Transactional
    public void execute(UUID linkId, List<UUID> userIds, String message) {
        if (userIds == null || userIds.isEmpty()) return;

        List<CreateNotificationDTO> dtos = userIds.stream()
                .map(userId -> new CreateNotificationDTO(userId, linkId, message))
                .toList();

        notificationRepository.saveAll(dtos);
        log.info("Saved {} notifications to inbox for link {}", dtos.size(), linkId);

        deliverWebPushUseCase.execute(userIds, message);
    }
}
