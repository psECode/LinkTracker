package backend.academy.linktracker.bot.application.context.track.usecases;

import backend.academy.linktracker.bot.domain.context.track.TrackContext;
import backend.academy.linktracker.bot.domain.context.track.TrackContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveTrackContextUseCase {
    private final TrackContextRepository repository;

    public void execute(TrackContext context) {
        repository.save(context);
    }
}
