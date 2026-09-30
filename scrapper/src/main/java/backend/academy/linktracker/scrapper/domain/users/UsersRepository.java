package backend.academy.linktracker.scrapper.domain.users;

import backend.academy.linktracker.scrapper.domain.users.dtos.CreateWebUserDto;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import java.util.Optional;
import java.util.UUID;

public interface UsersRepository {

    Optional<User> readByTelegramId(Long telegramId);

    Optional<User> readByEmail(String email);

    Optional<User> readByUUID(UUID uuid);

    Optional<User> saveTelegramUser(Long telegramId);

    Optional<User> saveWebUser(CreateWebUserDto userDto);

    Optional<User> delete(UUID uuid);
}
