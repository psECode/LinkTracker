package backend.academy.linktracker.scrapper.infrastructure.api.dtos;

import java.util.List;

public record ListLinksResponse(List<LinkResponse> links, Integer size) {}
