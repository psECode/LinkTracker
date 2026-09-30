package backend.academy.linktracker.scrapper.domain.notifications.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Notification(
        UUID id, UUID userId, UUID linkId, String message, OffsetDateTime createdAt, OffsetDateTime readAt) {}
