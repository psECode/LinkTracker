package backend.academy.linktracker.domain.users;

import backend.academy.linktracker.domain.users.dtos.CreateUserDto;
import backend.academy.linktracker.domain.users.entities.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository {
    Optional<User> readByChatId(Long chatId);

    Optional<User> save(CreateUserDto userDto);

    Optional<User> readByUUID(UUID uuid);

    Optional<User> delete(UUID uuid);
}
