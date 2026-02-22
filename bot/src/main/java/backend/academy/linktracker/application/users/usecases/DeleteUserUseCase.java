package backend.academy.linktracker.application.users.usecases;

import backend.academy.linktracker.domain.users.UsersRepository;
import backend.academy.linktracker.domain.users.entities.User;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteUserUseCase {
    private final UsersRepository usersRepository;

    public Optional<User> execute(UUID uuid) {
        return usersRepository.delete(uuid);
    }
}
