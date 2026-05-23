package backend.academy.linktracker.scrapper.domain.links.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateDateDTO(UUID id, OffsetDateTime lastUpdated, OffsetDateTime nextCheckAt) {}
