package backend.academy.linktracker.bot.infrastructure.repository.context;

import backend.academy.linktracker.bot.domain.context.untrack.UntrackContext;
import backend.academy.linktracker.bot.domain.context.untrack.UntrackContextRepository;
import backend.academy.linktracker.bot.properties.CacheProperties;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "redis")
public class RedisUntrackContextRepository implements UntrackContextRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties cacheProperties;
    private static final String PREFIX = "bot:untrack_context:";

    @Override
    public Optional<UntrackContext> read(Long chatId) {
        UntrackContext ctx = (UntrackContext) redisTemplate.opsForValue().get(PREFIX + chatId);
        return Optional.ofNullable(ctx);
    }

    @Override
    public void save(UntrackContext context) {
        redisTemplate.opsForValue().set(PREFIX + context.getChatId(), context, cacheProperties.getContextTtl());
    }

    @Override
    public void delete(Long chatId) {
        redisTemplate.delete(PREFIX + chatId);
    }
}
