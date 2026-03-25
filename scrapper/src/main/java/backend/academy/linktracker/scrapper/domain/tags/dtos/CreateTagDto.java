package backend.academy.linktracker.scrapper.domain.tags.dtos;

import java.util.UUID;

public record CreateTagDto(UUID subscriptionId, String tag) {}
