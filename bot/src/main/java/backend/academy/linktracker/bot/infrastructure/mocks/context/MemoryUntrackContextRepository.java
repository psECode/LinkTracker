package backend.academy.linktracker.bot.infrastructure.mocks.context;

import backend.academy.linktracker.bot.domain.context.untrack.UntrackContext;
import backend.academy.linktracker.bot.domain.context.untrack.UntrackContextRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryUntrackContextRepository implements UntrackContextRepository {

    private final Map<Long, UntrackContext> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<UntrackContext> read(Long chatId) {
        return Optional.ofNullable(storage.get(chatId));
    }

    @Override
    public void save(UntrackContext context) {
        storage.put(context.getChatId(), context);
    }

    @Override
    public void delete(Long chatId) {
        storage.remove(chatId);
    }
}
