package backend.academy.linktracker.scrapper.application.tags.usecases;

import backend.academy.linktracker.scrapper.domain.tags.TagRepository;
import backend.academy.linktracker.scrapper.domain.tags.entities.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadBySubscriptionUseCase {
    private final TagRepository tagRepository;

    public List<Tag> execute(UUID subscriptionId) {
        return tagRepository.readBySubscription(subscriptionId);
    }
}
