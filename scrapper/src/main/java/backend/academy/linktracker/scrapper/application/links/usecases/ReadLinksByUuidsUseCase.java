package backend.academy.linktracker.scrapper.application.links.usecases;

import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadLinksByUuidsUseCase {
    private final LinkRepository linkRepository;

    public List<Link> execute(Set<UUID> ids) {
        return linkRepository.readAllByIds(ids);
    }
}
