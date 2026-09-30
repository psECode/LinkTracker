package backend.academy.linktracker.bot.domain.api.dtos;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public record LinkResponse(UUID id, URI url, List<String> tags) {}
