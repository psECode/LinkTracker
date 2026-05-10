package backend.academy.linktracker.scrapper.domain.users;

import backend.academy.linktracker.scrapper.domain.users.dtos.CreateUserDto;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import java.util.Optional;
import java.util.UUID;

public interface UsersRepository {
    Optional<User> readByChatId(Long chatId);

    Optional<User> save(CreateUserDto userDto);

    Optional<User> readByUUID(UUID uuid);

    Optional<User> delete(UUID uuid);
}
