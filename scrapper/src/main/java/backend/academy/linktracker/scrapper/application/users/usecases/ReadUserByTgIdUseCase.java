package backend.academy.linktracker.scrapper.application.users.usecases;

import backend.academy.linktracker.scrapper.domain.users.UsersRepository;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadUserByTgIdUseCase {
    private final UsersRepository usersRepository;

    public Optional<User> execute(Long chatId) {
        return usersRepository.readByChatId(chatId);
    }
}
