package backend.academy.linktracker.scrapper.domain.api.dtos;

import java.util.List;

public record ListLinksResponse(List<LinkResponse> links, Integer size) {}
