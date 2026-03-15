package backend.academy.linktracker.scrapper.application.links.usecases;

import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadExpiredTrackedLinksUseCase {
    private final LinkRepository linkRepository;

    public List<Link> execute() {
        return linkRepository.readReadyToCheck(OffsetDateTime.now());
    }
}
