package backend.academy.linktracker.bot.application.context.usecases;

import backend.academy.linktracker.bot.domain.context.ContextRepository;
import backend.academy.linktracker.bot.domain.context.ContextType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadActiveContextUseCase {

    private final ContextRepository repository;

    public Optional<ContextType> execute(Long chatId) {
        return repository.read(chatId);
    }
}
