package backend.academy.linktracker.bot.application.context.usecases;

import backend.academy.linktracker.bot.domain.context.ContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteActiveContextUseCase {
    private final ContextRepository repository;

    public void execute(Long chatId) {
        repository.delete(chatId);
    }
}
