package backend.academy.linktracker.bot.domain.context.untrack;

import java.util.Optional;

public interface UntrackContextRepository {
    Optional<UntrackContext> read(Long chatId);

    void save(UntrackContext context);

    void delete(Long chatId);
}
