package backend.academy.linktracker.scrapper.infrastructure.api.dtos;

import java.util.List;
import java.util.UUID;

public record MarkReadRequest(List<UUID> ids, Boolean all) {}
