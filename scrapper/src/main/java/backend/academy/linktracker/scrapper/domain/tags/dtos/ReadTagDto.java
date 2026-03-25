package backend.academy.linktracker.scrapper.domain.tags.dtos;

import java.util.UUID;

public record ReadTagDto(UUID subscriptionId, String tag) {}
