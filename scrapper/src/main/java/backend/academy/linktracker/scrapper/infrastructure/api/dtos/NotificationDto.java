package backend.academy.linktracker.scrapper.infrastructure.api.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationDto(UUID id, UUID linkId, String message, OffsetDateTime createdAt, OffsetDateTime readAt) {}
