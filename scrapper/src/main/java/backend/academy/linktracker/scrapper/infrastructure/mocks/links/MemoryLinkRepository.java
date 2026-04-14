package backend.academy.linktracker.scrapper.infrastructure.mocks.links;

import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.domain.links.dtos.CreateTrackedLinkDTO;
import backend.academy.linktracker.scrapper.domain.links.dtos.UpdateDateDTO;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "mock")
public class MemoryLinkRepository implements LinkRepository {
    private final Map<UUID, Link> storage = new ConcurrentHashMap<>();



    @Override
    public Optional<Link> save(CreateTrackedLinkDTO dto) {
        Optional<Link> dataLink = readByUrl(dto.link());
        if (dataLink.isEmpty()) {
            Link link = Link.builder()
                    .id(UUID.randomUUID())
                    .url(dto.link())
                    .type(dto.type())
                    .lastUpdated(dto.lastCheckAt())
                    .checkInterval(dto.checkInterval())
                    .nextCheckAt(dto.nextCheckAt())
                    .build();
            storage.put(link.getId(), link);
            return Optional.of(link);
        }
        return dataLink;
    }

    @Override
    public Optional<Link> delete(UUID id) {
        Link removed = storage.remove(id);
        return Optional.ofNullable(removed);
    }

    @Override
    public Optional<Link> readById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Link> readReadyToCheck(OffsetDateTime now, int limit) {
        return storage.values().stream()
                .filter(s -> s.getNextCheckAt().isBefore(now))
                .toList();
    }

    @Override
    public List<Link> readAllByIds(Set<UUID> ids) {
        return storage.values().stream()
                .filter(link -> ids.contains(link.getId()))
                .toList();
    }

    @Override
    public Optional<Link> readByUrl(String url) {
        return storage.values().stream()
                .filter(link -> link.getUrl().equals(url))
                .findFirst();
    }

    @Override
    public void updateMetadata(UpdateDateDTO dto) {
        return;
    }
}
