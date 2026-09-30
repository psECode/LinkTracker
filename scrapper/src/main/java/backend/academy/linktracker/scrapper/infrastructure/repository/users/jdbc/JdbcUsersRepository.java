package backend.academy.linktracker.scrapper.infrastructure.repository.users.jdbc;

import backend.academy.linktracker.scrapper.domain.users.UsersRepository;
import backend.academy.linktracker.scrapper.domain.users.dtos.CreateWebUserDto;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import java.util.Map;
import java.util.Optional;
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
public class JdbcUsersRepository implements UsersRepository {
    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<User> rowMapper = (rs, n) -> User.builder()
            .id(rs.getObject("id", UUID.class))
            .telegramId(rs.getObject("telegram_id", Long.class))
            .email(rs.getString("email"))
            .passwordHash(rs.getString("password_hash"))
            .isActive(rs.getBoolean("is_active"))
            .build();

    @Override
    public Optional<User> readByTelegramId(Long telegramId) {
        return jdbc.query("SELECT * FROM users WHERE telegram_id = :id", Map.of("id", telegramId), rowMapper).stream()
                .findFirst();
    }

    @Override
    public Optional<User> readByEmail(String email) {
        return jdbc.query("SELECT * FROM users WHERE email = :email", Map.of("email", email), rowMapper).stream()
                .findFirst();
    }

    @Override
    public Optional<User> readByUUID(UUID id) {
        return jdbc.query("SELECT * FROM users WHERE id = :id", Map.of("id", id), rowMapper).stream()
                .findFirst();
    }

    @Override
    public Optional<User> saveTelegramUser(Long telegramId) {
        String sql = """
        INSERT INTO users (id, telegram_id, is_active)
        VALUES (:id, :telegramId, true)
        ON CONFLICT (telegram_id) DO UPDATE SET is_active = EXCLUDED.is_active
        RETURNING *
        """;
        return jdbc.query(sql, Map.of("id", UUID.randomUUID(), "telegramId", telegramId), rowMapper).stream()
                .findFirst();
    }

    @Override
    public Optional<User> saveWebUser(CreateWebUserDto dto) {
        String sql = """
        INSERT INTO users (id, email, password_hash, is_active)
        VALUES (:id, :email, :passwordHash, true)
        ON CONFLICT (email) DO NOTHING
        RETURNING *
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("email", dto.email())
                .addValue("passwordHash", dto.passwordHash());
        return jdbc.query(sql, params, rowMapper).stream().findFirst();
    }

    @Override
    public Optional<User> delete(UUID id) {
        return jdbc.query("DELETE FROM users WHERE id = :id RETURNING *", Map.of("id", id), rowMapper).stream()
                .findFirst();
    }
}
