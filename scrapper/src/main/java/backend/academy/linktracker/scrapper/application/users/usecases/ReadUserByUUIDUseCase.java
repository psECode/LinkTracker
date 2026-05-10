package backend.academy.linktracker.scrapper.application.users.usecases;

import backend.academy.linktracker.scrapper.domain.users.UsersRepository;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadUserByUUIDUseCase {
    private final UsersRepository usersRepository;

    public Optional<User> execute(UUID uuid) {
        return usersRepository.readByUUID(uuid);
    }
}
