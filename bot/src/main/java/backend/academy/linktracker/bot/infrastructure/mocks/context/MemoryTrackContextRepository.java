package backend.academy.linktracker.bot.infrastructure.mocks.context;

import backend.academy.linktracker.bot.domain.context.track.TrackContext;
import backend.academy.linktracker.bot.domain.context.track.TrackContextRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "mock")
public class MemoryTrackContextRepository implements TrackContextRepository {

    private final Map<Long, TrackContext> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<TrackContext> read(Long chatId) {
        return Optional.ofNullable(storage.get(chatId));
    }

    @Override
    public void save(TrackContext context) {
        storage.put(context.getChatId(), context);
    }

    @Override
    public void delete(Long chatId) {
        storage.remove(chatId);
    }
}
