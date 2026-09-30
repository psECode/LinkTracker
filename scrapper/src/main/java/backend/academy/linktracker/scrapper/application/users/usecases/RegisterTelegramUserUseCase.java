package backend.academy.linktracker.scrapper.application.users.usecases;

import backend.academy.linktracker.scrapper.domain.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterTelegramUserUseCase {

    private final UsersRepository usersRepository;

    public void execute(Long telegramId) {
        usersRepository.saveTelegramUser(telegramId);
    }
}
