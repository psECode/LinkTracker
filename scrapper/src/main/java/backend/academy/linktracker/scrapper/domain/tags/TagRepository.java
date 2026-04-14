package backend.academy.linktracker.scrapper.domain.tags;

import backend.academy.linktracker.scrapper.domain.tags.dtos.CreateTagDto;
import backend.academy.linktracker.scrapper.domain.tags.dtos.DeleteTagDto;
import backend.academy.linktracker.scrapper.domain.tags.dtos.ReadTagDto;
import backend.academy.linktracker.scrapper.domain.tags.entities.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository {

    Optional<Tag> add(CreateTagDto dto);

    List<Tag> readBySubscription(UUID subscriptionId);

    Optional<Tag> delete(DeleteTagDto dto);

    Optional<Tag> readBySubscriptionNTag(ReadTagDto dto);

    // List<Tag> deleteAllBySubscription(UUID subscriptionId);
}
