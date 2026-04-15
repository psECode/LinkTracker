package backend.academy.linktracker.scrapper.infrastructure.api.usecases;

import backend.academy.linktracker.scrapper.application.users.usecases.DeleteUserUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserService;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnregisterUserUseCase {
    private final ReadUserService readUserService;
    private final DeleteUserUseCase deleteUserUseCase;

    @Transactional
    public void execute(Long chatId) {
        User user = readUserService.readByChatId(chatId).orElseThrow(() -> new UserNotFoundException(chatId));
        deleteUserUseCase.execute(user.getId());
    }
}
