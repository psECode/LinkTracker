package backend.academy.linktracker.scrapper.infrastructure.api.checkers;

import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import java.util.List;

public interface LinkChecker {
    LinkType getType();

    List<UpdateDescription> checkUpdates(Link link);
}
