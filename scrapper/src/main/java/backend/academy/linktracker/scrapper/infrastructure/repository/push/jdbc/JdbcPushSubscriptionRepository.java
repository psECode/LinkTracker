package backend.academy.linktracker.scrapper.infrastructure.repository.push.jdbc;

import backend.academy.linktracker.scrapper.domain.push.PushSubscription;
import backend.academy.linktracker.scrapper.domain.push.PushSubscriptionRepository;
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
public class JdbcPushSubscriptionRepository implements PushSubscriptionRepository {
    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<PushSubscription> rowMapper = (rs, n) -> new PushSubscription(
            rs.getObject("id", UUID.class),
            rs.getObject("user_id", UUID.class),
            rs.getString("endpoint"),
            rs.getString("p256dh"),
            rs.getString("auth"));

    @Override
    public void save(PushSubscription subscription) {
        jdbc.update(
                "INSERT INTO push_subscriptions (id, user_id, endpoint, p256dh, auth) "
                        + "VALUES (:id, :userId, :endpoint, :p256dh, :auth) "
                        + "ON CONFLICT (endpoint) DO UPDATE SET "
                        + "user_id = EXCLUDED.user_id, p256dh = EXCLUDED.p256dh, auth = EXCLUDED.auth",
                new MapSqlParameterSource()
                        .addValue("id", subscription.id())
                        .addValue("userId", subscription.userId())
                        .addValue("endpoint", subscription.endpoint())
                        .addValue("p256dh", subscription.p256dh())
                        .addValue("auth", subscription.auth()));
    }

    @Override
    public List<PushSubscription> findByUser(UUID userId) {
        return jdbc.query(
                "SELECT * FROM push_subscriptions WHERE user_id = :userId",
                Map.of("userId", userId),
                rowMapper);
    }

    @Override
    public void deleteByEndpoint(String endpoint) {
        jdbc.update(
                "DELETE FROM push_subscriptions WHERE endpoint = :endpoint",
                Map.of("endpoint", endpoint));
    }
}
