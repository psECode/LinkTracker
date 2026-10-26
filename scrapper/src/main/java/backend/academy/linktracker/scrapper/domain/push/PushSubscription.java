package backend.academy.linktracker.scrapper.domain.push;

import java.util.UUID;

public record PushSubscription(UUID id, UUID userId, String endpoint, String p256dh, String auth) {}
