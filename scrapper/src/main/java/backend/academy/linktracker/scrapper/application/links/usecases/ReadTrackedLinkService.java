package backend.academy.linktracker.scrapper.application.links.usecases;

import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReadTrackedLinkService {
    private final LinkRepository linkRepository;

    public List<Link> readByUUIDs(Set<UUID> ids) {
        return linkRepository.readAllByIds(ids);
    }

    public Optional<Link> readByUrl(String url) {
        return linkRepository.readByUrl(url);
    }

    public Optional<Link> readByUUID(UUID id) {
        return linkRepository.readById(id);
    }

    public List<Link> readExpiredLinks(int limit) {
        return linkRepository.readReadyToCheck(OffsetDateTime.now(), limit);
    }
}
