package backend.academy.linktracker.scrapper.application.links.usecases;

import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadTrackedLinkByUrlUseCase {
    private final LinkRepository linkRepository;

    public Optional<Link> execute(String url) {
        return linkRepository.readByUrl(url);
    }
}
