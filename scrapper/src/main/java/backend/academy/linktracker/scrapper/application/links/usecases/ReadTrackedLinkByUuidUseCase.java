package backend.academy.linktracker.scrapper.application.links.usecases;

import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadTrackedLinkByUuidUseCase {
    private final LinkRepository linkRepository;

    public Optional<Link> execute(UUID id) {
        return linkRepository.readById(id);
    }
}
