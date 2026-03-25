package backend.academy.linktracker.scrapper.infrastructure.repository.users.jdbc;

import backend.academy.linktracker.scrapper.domain.users.UsersRepository;
import backend.academy.linktracker.scrapper.domain.users.dtos.CreateUserDto;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jdbc")
public class JdbcUsersRepository implements UsersRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final RowMapper<User> rowMapper = (rs, n) -> User.builder()
            .id(rs.getObject("id", UUID.class))
            .chatId(rs.getLong("telegram_id"))
            .isActive(rs.getBoolean("is_active"))
            .build();

    @Override
    public Optional<User> readByChatId(Long id) {
        return jdbc.query("SELECT * FROM users WHERE telegram_id = :id", Map.of("id", id), rowMapper).stream()
                .findFirst();
    }

    @Override
    public Optional<User> readByUUID(UUID id) {
        return jdbc.query("SELECT * FROM users WHERE id = :id", Map.of("id", id), rowMapper).stream()
                .findFirst();
    }

    @Override
    public Optional<User> save(CreateUserDto dto) {
        String sql = """
        INSERT INTO users (id, telegram_id, is_active)
        VALUES (:id, :chatId, true)
        ON CONFLICT (telegram_id) DO UPDATE SET is_active = EXCLUDED.is_active
        RETURNING *
        """;
        return jdbc.query(sql, Map.of("id", UUID.randomUUID(), "chatId", dto.chatId()), rowMapper).stream()
                .findFirst();
    }

    @Override
    public Optional<User> delete(UUID id) {
        return jdbc.query("DELETE FROM users WHERE id = :id RETURNING *", Map.of("id", id), rowMapper).stream()
                .findFirst();
    }
}
