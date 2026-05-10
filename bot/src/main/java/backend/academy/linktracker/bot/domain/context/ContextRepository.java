package backend.academy.linktracker.bot.domain.context;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface ContextRepository {

    Optional<ContextType> read(Long chatId);

    void save(Long chatId, ContextType type);

    void delete(Long chatId);
}
