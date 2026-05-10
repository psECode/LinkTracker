package backend.academy.linktracker.bot.infrastructure.mocks.context;

import backend.academy.linktracker.bot.domain.context.ContextRepository;
import backend.academy.linktracker.bot.domain.context.ContextType;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryContextRepository implements ContextRepository {
    private final Map<Long, ContextType> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<ContextType> read(Long chatId) {
        return Optional.ofNullable(storage.get(chatId));
    }

    @Override
    public void save(Long chatId, ContextType type) {
        storage.put(chatId, type);
    }

    @Override
    public void delete(Long chatId) {
        storage.remove(chatId);
    }
}
