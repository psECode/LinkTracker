package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.domain.links.LinkType;
import java.time.OffsetDateTime;

public interface LinkChecker {
    LinkType getType();

    OffsetDateTime getLastUpdatedDate(String url);
}
