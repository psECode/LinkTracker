package backend.academy.linktracker.infrastructure.mocks.users;

import backend.academy.linktracker.domain.users.UsersRepository;
import backend.academy.linktracker.domain.users.dtos.CreateUserDto;
import backend.academy.linktracker.domain.users.entities.User;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryUsersRepository implements UsersRepository {

    private final Map<Long, User> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<User> readByChatId(Long chatId) {
        return Optional.ofNullable(storage.get(chatId));
    }

    @Override
    public Optional<User> readByUUID(UUID id) {
        return storage.values().stream().filter(user -> id.equals(user.getId())).findFirst();
    }

    @Override
    public Optional<User> save(CreateUserDto dto) {
        if (storage.containsKey(dto.getChatId())) {
            return Optional.empty();
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(true)
                .chatId(dto.getChatId())
                .build();

        storage.put(user.getChatId(), user);
        return Optional.of(user);
    }

    @Override
    public Optional<User> delete(UUID uuid) {
        Optional<User> userToDelete = readByUUID(uuid);

        return userToDelete.map(user -> {
            storage.remove(user.getChatId());
            return user;
        });
    }
}
