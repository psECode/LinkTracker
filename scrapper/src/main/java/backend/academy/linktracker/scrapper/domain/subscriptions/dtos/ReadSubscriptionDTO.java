package backend.academy.linktracker.scrapper.domain.subscriptions.dtos;

import java.util.UUID;

public record ReadSubscriptionDTO(UUID userId, UUID linkId) {}
