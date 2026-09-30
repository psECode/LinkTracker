package backend.academy.linktracker.scrapper.users.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.linktracker.scrapper.domain.users.dtos.CreateWebUserDto;
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
    void createTelegramUserTest() {
        Optional<User> saved = repository.saveTelegramUser(12345L);

        Optional<User> found = repository.readByTelegramId(12345L);

        assertThat(found).isPresent();
        assertThat(found.equals(saved));
    }

    @Test
    void upsertTelegramUserKeepsSameIdTest() {
        Optional<User> first = repository.saveTelegramUser(12345L);
        Optional<User> second = repository.saveTelegramUser(12345L);

        assertThat(second).isPresent();
        assertThat(second.get().getId()).isEqualTo(first.get().getId());
    }

    @Test
    void readByUUIDTest() {
        User user = repository.saveTelegramUser(111L).get();
        UUID uuid = user.getId();

        Optional<User> found = repository.readByUUID(uuid);

        assertThat(found).isPresent();
        assertThat(found.get().getTelegramId()).isEqualTo(111L);
    }

    @Test
    void deleteTest() {
        User user = repository.saveTelegramUser(999L).get();
        UUID uuid = user.getId();

        Optional<User> deleted = repository.delete(uuid);

        assertThat(deleted).isPresent();
        assertThat(repository.readByUUID(uuid)).isEmpty();
    }

    @Test
    void saveWebUserTest() {
        Optional<User> saved = repository.saveWebUser(new CreateWebUserDto("user@example.com", "hash"));

        assertThat(saved).isPresent();
        assertThat(saved.get().getEmail()).isEqualTo("user@example.com");
        assertThat(repository.readByEmail("user@example.com")).isPresent();
    }

    @Test
    void duplicateEmailTest() {
        repository.saveWebUser(new CreateWebUserDto("user@example.com", "hash"));

        Optional<User> second = repository.saveWebUser(new CreateWebUserDto("user@example.com", "hash2"));

        assertThat(second).isEmpty();
    }
}
