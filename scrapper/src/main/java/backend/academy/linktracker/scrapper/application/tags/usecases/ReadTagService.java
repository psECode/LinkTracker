package backend.academy.linktracker.scrapper.application.tags.usecases;

import backend.academy.linktracker.scrapper.domain.tags.TagRepository;
import backend.academy.linktracker.scrapper.domain.tags.dtos.ReadTagDto;
import backend.academy.linktracker.scrapper.domain.tags.entities.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReadTagService {
    private final TagRepository tagRepository;

    public Optional<Tag> read(ReadTagDto dto) {
        return tagRepository.readBySubscriptionNTag(dto);
    }

    public List<Tag> readBySubscriptionUUID(UUID subscriptionId) {
        return tagRepository.readBySubscription(subscriptionId);
    }
}
