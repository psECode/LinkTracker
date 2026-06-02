package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.domain.links.LinkType;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScrapperMetricsConfig {

    @Bean
    public Gauge githubLinksGauge(MeterRegistry registry, LinkRepository repo) {
        return Gauge.builder("links_on_track", repo, r -> r.countByType(LinkType.GITHUB))
                .tag("tracked_source", "github")
                .register(registry);
    }

    @Bean
    public Gauge soLinksGauge(MeterRegistry registry, LinkRepository repo) {
        return Gauge.builder("links_on_track", repo, r -> r.countByType(LinkType.STACKOVERFLOW))
                .tag("tracked_source", "stackoverflow")
                .register(registry);
    }
}
