package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.LinkChecker;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.LinkUpdateReport;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkUpdater {
    private final Map<LinkType, LinkChecker> checkers;

    public LinkUpdateReport process(Link link) {
        try {
            var updates = checkers.get(link.getType()).checkUpdates(link);
            return new LinkUpdateReport(link, updates, null);
        } catch (Exception e) {
            log.warn("Link {} check failed: {}", link.getUrl(), e.getMessage(), e);
            return new LinkUpdateReport(link, List.of(), e.getMessage());
        }
    }
}
