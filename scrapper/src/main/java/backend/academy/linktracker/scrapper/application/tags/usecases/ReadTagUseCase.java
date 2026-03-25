package backend.academy.linktracker.scrapper.application.tags.usecases;

import backend.academy.linktracker.scrapper.domain.tags.TagRepository;
import backend.academy.linktracker.scrapper.domain.tags.dtos.ReadTagDto;
import backend.academy.linktracker.scrapper.domain.tags.entities.Tag;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadTagUseCase {
    private final TagRepository tagRepository;

    public Optional<Tag> execute(ReadTagDto dto) {
        return tagRepository.readBySubscriptionNTag(dto);
    }
}
