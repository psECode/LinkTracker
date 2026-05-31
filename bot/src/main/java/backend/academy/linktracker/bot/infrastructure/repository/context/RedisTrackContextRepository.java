package backend.academy.linktracker.bot.infrastructure.repository.context;

import backend.academy.linktracker.bot.domain.context.track.TrackContext;
import backend.academy.linktracker.bot.domain.context.track.TrackContextRepository;
import backend.academy.linktracker.bot.properties.CacheProperties;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "redis")
public class RedisTrackContextRepository implements TrackContextRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "bot:track_context:";
    private final CacheProperties properties;

    @Override
    public Optional<TrackContext> read(Long chatId) {
        TrackContext ctx = (TrackContext) redisTemplate.opsForValue().get(PREFIX + chatId);
        return Optional.ofNullable(ctx);
    }

    @Override
    public void save(TrackContext context) {
        redisTemplate.opsForValue().set(PREFIX + context.getChatId(), context, properties.getContextTtl());
    }

    @Override
    public void delete(Long chatId) {
        redisTemplate.delete(PREFIX + chatId);
    }
}
