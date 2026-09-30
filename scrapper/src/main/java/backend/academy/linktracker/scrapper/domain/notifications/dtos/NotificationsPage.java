package backend.academy.linktracker.scrapper.domain.notifications.dtos;

import backend.academy.linktracker.scrapper.domain.notifications.entities.Notification;
import java.util.List;

public record NotificationsPage(List<Notification> items, long unreadCount) {}
