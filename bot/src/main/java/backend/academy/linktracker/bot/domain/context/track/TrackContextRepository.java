package backend.academy.linktracker.bot.domain.context.track;

import java.util.Optional;

public interface TrackContextRepository {
    Optional<TrackContext> read(Long chatId);

    void save(TrackContext context);

    void delete(Long chatId);
}
