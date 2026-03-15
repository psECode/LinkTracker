package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.domain.links.LinkType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CheckerConfig {

    @Bean
    public Map<LinkType, LinkChecker> updaters(List<LinkChecker> updaterList) {
        return updaterList.stream().collect(Collectors.toMap(LinkChecker::getType, u -> u));
    }
}
