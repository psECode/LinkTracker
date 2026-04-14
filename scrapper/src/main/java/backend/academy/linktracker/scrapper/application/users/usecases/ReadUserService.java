package backend.academy.linktracker.scrapper.application.users.usecases;

import backend.academy.linktracker.scrapper.domain.users.UsersRepository;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReadUserService {
    private final UsersRepository usersRepository;

    public Optional<User> readByChatId(Long chatId) {
        return usersRepository.readByChatId(chatId);
    }

    public Optional<User> readByUUID(UUID uuid) {
        return usersRepository.readByUUID(uuid);
    }
}
