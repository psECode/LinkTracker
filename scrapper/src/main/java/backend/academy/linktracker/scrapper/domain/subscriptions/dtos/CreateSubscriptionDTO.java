package backend.academy.linktracker.scrapper.domain.subscriptions.dtos;

import java.util.List;
import java.util.UUID;

public record CreateSubscriptionDTO(UUID userId, UUID linkId, List<String> tags) {}
