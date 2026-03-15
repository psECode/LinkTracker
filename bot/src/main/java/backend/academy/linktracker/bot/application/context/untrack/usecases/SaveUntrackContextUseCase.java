package backend.academy.linktracker.bot.application.context.untrack.usecases;

import backend.academy.linktracker.bot.domain.context.untrack.UntrackContext;
import backend.academy.linktracker.bot.domain.context.untrack.UntrackContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveUntrackContextUseCase {
    private final UntrackContextRepository repository;

    public void execute(UntrackContext context) {
        repository.save(context);
    }
}
