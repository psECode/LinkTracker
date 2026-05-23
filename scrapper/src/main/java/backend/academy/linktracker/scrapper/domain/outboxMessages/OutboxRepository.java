package backend.academy.linktracker.scrapper.domain.outboxMessages;

import backend.academy.linktracker.scrapper.domain.outboxMessages.dtos.CreateOutboxMessageDTO;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository {
    OutboxMessage save(CreateOutboxMessageDTO dto);

    List<OutboxMessage> readPending(int limit);

    void updateStatus(UUID id, OutboxStatus status);

    void delete(UUID id);
}
