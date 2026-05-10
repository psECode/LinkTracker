package backend.academy.linktracker.scrapper.domain.subscriptions.entities;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Builder
public class Subscription {
    private final UUID id;

    private final UUID userId;

    private final UUID linkId;

    private final List<String> tags;
}
