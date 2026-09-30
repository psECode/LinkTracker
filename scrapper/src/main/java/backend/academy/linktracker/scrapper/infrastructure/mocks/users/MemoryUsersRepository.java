package backend.academy.linktracker.scrapper.infrastructure.mocks.users;

import backend.academy.linktracker.scrapper.domain.users.UsersRepository;
import backend.academy.linktracker.scrapper.domain.users.dtos.CreateWebUserDto;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "mock")
public class MemoryUsersRepository implements UsersRepository {

    private final Map<UUID, User> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<User> readByTelegramId(Long telegramId) {
        return storage.values().stream()
                .filter(user -> telegramId.equals(user.getTelegramId()))
                .findFirst();
    }

    @Override
    public Optional<User> readByEmail(String email) {
        return storage.values().stream()
                .filter(user -> user.getEmail() != null && user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<User> readByUUID(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<User> saveTelegramUser(Long telegramId) {
        User existing = readByTelegramId(telegramId).orElse(null);
        if (existing != null) {
            existing.setIsActive(true);
            return Optional.of(existing);
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .telegramId(telegramId)
                .isActive(true)
                .build();

        storage.put(user.getId(), user);
        return Optional.of(user);
    }

    @Override
    public Optional<User> saveWebUser(CreateWebUserDto dto) {
        if (readByEmail(dto.email()).isPresent()) {
            return Optional.empty();
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(dto.email())
                .passwordHash(dto.passwordHash())
                .isActive(true)
                .build();

        storage.put(user.getId(), user);
        return Optional.of(user);
    }

    @Override
    public Optional<User> delete(UUID uuid) {
        return Optional.ofNullable(storage.remove(uuid));
    }
}
