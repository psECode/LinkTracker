package backend.academy.linktracker.scrapper.users.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.linktracker.scrapper.domain.users.dtos.CreateUserDto;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.mocks.users.MemoryUsersRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemoryUsersRepositoryTest {

    private MemoryUsersRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MemoryUsersRepository();
    }

    @Test
    void CreateTest() {
        Optional<User> saved = createSimpleUser(repository, 12345L);

        Optional<User> found = repository.readByChatId(12345L);

        assertThat(found).isPresent();
        assertThat(found.equals(saved));
    }

    @Test
    void AlreadyExistsTest() {
        Optional<User> first = createSimpleUser(repository, 12345L);
        Optional<User> user = createSimpleUser(repository, 12345L);
        assertThat(user).isEmpty();
    }

    @Test
    void readByUUIDTest() {
        User user = createSimpleUser(repository, 111L).get();
        UUID uuid = user.getId();

        Optional<User> found = repository.readByUUID(uuid);

        assertThat(found).isPresent();
        assertThat(found.get().getChatId()).isEqualTo(111L);
    }

    @Test
    void DeleteTest() {
        User user = createSimpleUser(repository, 999L).get();
        UUID uuid = user.getId();

        Optional<User> deleted = repository.delete(uuid);

        assertThat(deleted).isPresent();
        assertThat(repository.readByUUID(uuid)).isEmpty();
    }

    public Optional<User> createSimpleUser(MemoryUsersRepository repository, Long chatId) {
        CreateUserDto dto = new CreateUserDto(chatId);
        return repository.save(dto);
    }
}
