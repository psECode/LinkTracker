package backend.academy.linktracker.bot.application.context.untrack.usecases;

import backend.academy.linktracker.bot.domain.context.untrack.UntrackContext;
import backend.academy.linktracker.bot.domain.context.untrack.UntrackContextRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadUntrackContextUseCase {
    private final UntrackContextRepository repository;

    public Optional<UntrackContext> execute(Long chatId) {
        return repository.read(chatId);
    }
}
