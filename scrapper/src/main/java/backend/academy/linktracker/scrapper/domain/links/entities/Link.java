package backend.academy.linktracker.scrapper.domain.links.entities;

import backend.academy.linktracker.scrapper.domain.links.LinkType;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Link {
    private final UUID id;

    private final String url;

    private final LinkType type;

    private OffsetDateTime lastUpdated;

    private OffsetDateTime nextCheckAt;

    private Duration checkInterval;
}
