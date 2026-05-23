package backend.academy.linktracker.scrapper.domain.outboxMessages;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class OutboxMessage {
    private final UUID id;
    private final String payload;
    private OutboxStatus status;
    private final OffsetDateTime createdAt;
}
