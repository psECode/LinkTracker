package backend.academy.linktracker.scrapper.infrastructure.repository.notifications.jdbc;

import backend.academy.linktracker.scrapper.domain.notifications.NotificationRepository;
import backend.academy.linktracker.scrapper.domain.notifications.dtos.CreateNotificationDTO;
import backend.academy.linktracker.scrapper.domain.notifications.entities.Notification;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jdbc")
public class JdbcNotificationRepository implements NotificationRepository {
    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<Notification> rowMapper = (rs, n) -> new Notification(
            rs.getObject("id", UUID.class),
            rs.getObject("user_id", UUID.class),
            rs.getObject("link_id", UUID.class),
            rs.getString("message"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("read_at", OffsetDateTime.class));

    @Override
    @Transactional
    public void saveAll(List<CreateNotificationDTO> dtos) {
        if (dtos.isEmpty()) {
            return;
        }

        SqlParameterSource[] batch = dtos.stream()
                .map(dto -> new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID())
                        .addValue("userId", dto.userId())
                        .addValue("linkId", dto.linkId())
                        .addValue("message", dto.message())
                        .addValue("createdAt", OffsetDateTime.now()))
                .toArray(SqlParameterSource[]::new);

        jdbc.batchUpdate(
                "INSERT INTO notifications (id, user_id, link_id, message, created_at) "
                        + "VALUES (:id, :userId, :linkId, :message, :createdAt)",
                batch);
    }

    @Override
    public List<Notification> readByUser(UUID userId, boolean onlyUnread, int limit) {
        String sql = onlyUnread
                ? "SELECT * FROM notifications WHERE user_id = :u AND read_at IS NULL "
                        + "ORDER BY created_at DESC LIMIT :limit"
                : "SELECT * FROM notifications WHERE user_id = :u ORDER BY created_at DESC LIMIT :limit";
        return jdbc.query(sql, new MapSqlParameterSource().addValue("u", userId).addValue("limit", limit), rowMapper);
    }

    @Override
    @Transactional
    public int markRead(UUID userId, List<UUID> ids) {
        if (ids.isEmpty()) {
            return 0;
        }
        return jdbc.update(
                "UPDATE notifications SET read_at = now() WHERE user_id = :u AND id IN (:ids) AND read_at IS NULL",
                new MapSqlParameterSource().addValue("u", userId).addValue("ids", ids));
    }

    @Override
    @Transactional
    public int markAllRead(UUID userId) {
        return jdbc.update(
                "UPDATE notifications SET read_at = now() WHERE user_id = :u AND read_at IS NULL", Map.of("u", userId));
    }

    @Override
    public long countUnread(UUID userId) {
        Long count = jdbc.queryForObject(
                "SELECT count(*) FROM notifications WHERE user_id = :u AND read_at IS NULL",
                Map.of("u", userId),
                Long.class);
        return count == null ? 0 : count;
    }
}
