package backend.academy.linktracker.bot.domain.api.dtos;

import java.net.URI;
import java.util.List;

public record AddLinkRequest(URI link, List<String> tags) {}
