package backend.academy.linktracker.bot.application.context.track.usecases;

import backend.academy.linktracker.bot.domain.context.track.TrackContext;
import backend.academy.linktracker.bot.domain.context.track.TrackContextRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadTrackContextUseCase {
    private final TrackContextRepository repository;

    public Optional<TrackContext> execute(Long chatId) {
        return repository.read(chatId);
    }
}
