package backend.academy.linktracker.scrapper.domain.links.dtos;

import java.util.UUID;

public record UpdateTimeDTO(UUID linkId, Boolean updated) {}
