package backend.academy.linktracker.scrapper.domain.links.dtos;

import backend.academy.linktracker.scrapper.domain.links.LinkType;
import java.time.Duration;
import java.time.OffsetDateTime;

public record CreateTrackedLinkDTO(
        String link, OffsetDateTime nextCheckAt, LinkType type, OffsetDateTime lastCheckAt, Duration checkInterval) {}
