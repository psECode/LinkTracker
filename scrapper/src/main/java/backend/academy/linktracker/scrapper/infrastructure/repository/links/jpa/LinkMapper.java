package backend.academy.linktracker.scrapper.infrastructure.repository.links.jpa;

import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import org.springframework.stereotype.Component;

@Component
public class LinkMapper {
    public Link toDomain(LinkJpaEntity e) {
        return Link.builder()
                .id(e.getId())
                .url(e.getUrl())
                .type(e.getType())
                .lastUpdated(e.getLastUpdated())
                .nextCheckAt(e.getNextCheckAt())
                .checkInterval(e.getCheckInterval())
                .build();
    }

    public LinkJpaEntity toEntity(Link d) {
        return new LinkJpaEntity(
                d.getId(), d.getUrl(), d.getType(), d.getLastUpdated(), d.getNextCheckAt(), d.getCheckInterval());
    }
}
