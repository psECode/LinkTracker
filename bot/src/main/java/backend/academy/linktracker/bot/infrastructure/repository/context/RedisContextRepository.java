package backend.academy.linktracker.bot.infrastructure.repository.context;

import backend.academy.linktracker.bot.domain.context.ContextRepository;
import backend.academy.linktracker.bot.domain.context.ContextType;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "redis")
public class RedisContextRepository implements ContextRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "bot:active_context:";
    private static final Duration TTL = Duration.ofMinutes(30);

    @Override
    public Optional<ContextType> read(Long chatId) {
        Object val = redisTemplate.opsForValue().get(PREFIX + chatId);
        return Optional.ofNullable(val).map(v -> ContextType.valueOf(v.toString()));
    }

    @Override
    public void save(Long chatId, ContextType type) {
        redisTemplate.opsForValue().set(PREFIX + chatId, type.name(), TTL);
    }

    @Override
    public void delete(Long chatId) {
        redisTemplate.delete(PREFIX + chatId);
    }
}
