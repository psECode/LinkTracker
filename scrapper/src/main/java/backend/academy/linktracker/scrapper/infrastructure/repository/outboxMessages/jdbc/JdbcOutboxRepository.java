package backend.academy.linktracker.scrapper.infrastructure.repository.outboxMessages.jdbc;

import backend.academy.linktracker.scrapper.domain.outboxMessages.OutboxMessage;
import backend.academy.linktracker.scrapper.domain.outboxMessages.OutboxRepository;
import backend.academy.linktracker.scrapper.domain.outboxMessages.OutboxStatus;
import backend.academy.linktracker.scrapper.domain.outboxMessages.dtos.CreateOutboxMessageDTO;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jdbc")
public class JdbcOutboxRepository implements OutboxRepository {
    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<OutboxMessage> rowMapper = (rs, n) -> OutboxMessage.builder()
            .id(rs.getObject("id", UUID.class))
            .payload(rs.getString("payload"))
            .status(OutboxStatus.valueOf(rs.getString("status")))
            .createdAt(rs.getObject("created_at", OffsetDateTime.class))
            .build();

    @Override
    public OutboxMessage save(CreateOutboxMessageDTO dto) {
        String sql = "INSERT INTO outbox_messages (id, payload, status, created_at) "
                + "VALUES (:id, :p, :s, :c) RETURNING *";
        return jdbc.queryForObject(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID())
                        .addValue("p", dto.payload())
                        .addValue("s", OutboxStatus.PENDING.name())
                        .addValue("c", OffsetDateTime.now()),
                rowMapper);
    }

    @Override
    public List<OutboxMessage> readPending(int limit) {
        String sql = "SELECT * FROM outbox_messages WHERE status = 'PENDING' " + "ORDER BY created_at ASC LIMIT :limit";
        return jdbc.query(sql, Map.of("limit", limit), rowMapper);
    }

    @Override
    public void updateStatus(UUID id, OutboxStatus status) {
        String sql = "UPDATE outbox_messages SET status = :s WHERE id = :id";
        jdbc.update(sql, Map.of("s", status.name(), "id", id));
    }

    @Override
    public void delete(UUID id) {
        jdbc.update("DELETE FROM outbox_messages WHERE id = :id", Map.of("id", id));
    }
}
