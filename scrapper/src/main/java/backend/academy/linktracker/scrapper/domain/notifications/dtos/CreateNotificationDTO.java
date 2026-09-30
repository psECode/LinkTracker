package backend.academy.linktracker.scrapper.domain.notifications.dtos;

import java.util.UUID;

public record CreateNotificationDTO(UUID userId, UUID linkId, String message) {}
