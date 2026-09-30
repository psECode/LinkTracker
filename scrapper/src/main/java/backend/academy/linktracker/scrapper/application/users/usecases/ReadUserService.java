package backend.academy.linktracker.scrapper.application.users.usecases;

import backend.academy.linktracker.scrapper.domain.users.UsersRepository;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadUserService {
    private final UsersRepository usersRepository;

    public Optional<User> readByTelegramId(Long telegramId) {
        return usersRepository.readByTelegramId(telegramId);
    }

    public Optional<User> readByEmail(String email) {
        return usersRepository.readByEmail(email);
    }

    public Optional<User> readByUUID(UUID uuid) {
        return usersRepository.readByUUID(uuid);
    }
}
