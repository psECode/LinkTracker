package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.logging.LogArgs;
import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.LinkChecker;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.LinkUpdateReport;
import io.micrometer.core.instrument.MeterRegistry;
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
    private final MeterRegistry meterRegistry;

    public LinkUpdateReport process(Link link) {
        String source = link.getType().name().toLowerCase();
        try {
            var updates = checkers.get(link.getType()).checkUpdates(link);
            meterRegistry.counter("link_checks_total", "source", source, "outcome", "success").increment();
            return new LinkUpdateReport(link, updates, null);
        } catch (Exception e) {
            meterRegistry.counter("link_checks_total", "source", source, "outcome", "failure").increment();
            log.warn("Link check failed", LogArgs.of("link", link.getUrl(), "error", e.getMessage()), e);
            return new LinkUpdateReport(link, List.of(), e.getMessage());
        }
    }
}
