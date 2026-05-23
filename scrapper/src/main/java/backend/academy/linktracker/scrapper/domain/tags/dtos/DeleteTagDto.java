package backend.academy.linktracker.scrapper.domain.tags.dtos;

import java.util.UUID;

public record DeleteTagDto(UUID subscriptionId, String tag) {}
