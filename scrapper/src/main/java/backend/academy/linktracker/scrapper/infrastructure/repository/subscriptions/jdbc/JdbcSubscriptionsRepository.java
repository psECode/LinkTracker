package backend.academy.linktracker.scrapper.infrastructure.repository.subscriptions.jdbc;

import backend.academy.linktracker.scrapper.domain.subscriptions.SubscriptionRepository;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.CreateSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.ReadSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import jakarta.transaction.Transactional;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jdbc")
public class JdbcSubscriptionsRepository implements SubscriptionRepository {
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    @Transactional
    public Optional<Subscription> save(CreateSubscriptionDTO dto) {
        UUID subId = UUID.randomUUID();

        String sqlSub = "INSERT INTO subscriptions (id, user_id, link_id) VALUES (:id, :u, :l) RETURNING *";
        jdbc.query(sqlSub, Map.of("id", subId, "u", dto.userId(), "l", dto.linkId()), this::mapWithTags);

        if (dto.tags() != null && !dto.tags().isEmpty()) {
            String sqlTags = "INSERT INTO subscription_tags (subscription_id, tag) VALUES (:id, :tag)";

            SqlParameterSource[] batch = dto.tags().stream()
                    .map(tag ->
                            new MapSqlParameterSource().addValue("id", subId).addValue("tag", tag))
                    .toArray(SqlParameterSource[]::new);

            jdbc.batchUpdate(sqlTags, batch);
        }

        return read(new ReadSubscriptionDTO(dto.userId(), dto.linkId()));
    }

    @Override
    public Optional<Subscription> read(ReadSubscriptionDTO dto) {
        return jdbc
                .query(
                        "SELECT * FROM subscriptions WHERE user_id = :u AND link_id = :l",
                        Map.of("u", dto.userId(), "l", dto.linkId()),
                        this::mapWithTags)
                .stream()
                .findFirst();
    }

    @Override
    public List<Subscription> readByUser(UUID uId) {
        return jdbc.query("SELECT * FROM subscriptions WHERE user_id = :u", Map.of("u", uId), this::mapWithTags);
    }

    @Override
    public List<Subscription> readByLink(UUID lId) {
        return jdbc.query("SELECT * FROM subscriptions WHERE link_id = :l", Map.of("l", lId), this::mapWithTags);
    }

    @Override
    public void delete(UUID id) {
        jdbc.update("DELETE FROM subscriptions WHERE id = :id", Map.of("id", id));
    }

    @SuppressWarnings("unused")
    private Subscription mapWithTags(ResultSet rs, int rowNum) throws SQLException {
        UUID id = rs.getObject("id", UUID.class);
        List<String> tags = jdbc.queryForList(
                "SELECT tag FROM subscription_tags WHERE subscription_id = :id", Map.of("id", id), String.class);
        return Subscription.builder()
                .id(id)
                .userId(rs.getObject("user_id", UUID.class))
                .linkId(rs.getObject("link_id", UUID.class))
                .tags(tags)
                .build();
    }
}
