package backend.academy.linktracker.scrapper.domain.tags.entities;

import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Builder
public class Tag {
    private final UUID id;

    private final UUID subscriptionId;

    private final String tagString;
}
