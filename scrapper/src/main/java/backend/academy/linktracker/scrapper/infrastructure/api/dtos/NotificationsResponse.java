package backend.academy.linktracker.scrapper.infrastructure.api.dtos;

import java.util.List;

public record NotificationsResponse(List<NotificationDto> items, long unreadCount) {}
