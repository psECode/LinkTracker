package backend.academy.linktracker.scrapper.infrastructure.repository.users.jpa;

import backend.academy.linktracker.scrapper.domain.users.UsersRepository;
import backend.academy.linktracker.scrapper.domain.users.dtos.CreateUserDto;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jpa")
public class JpaUsersRepository implements UsersRepository {
    private final UserJpaRepositoryInterface jpa;
    private final UserMapper mapper;

    @Override
    public Optional<User> readByChatId(Long chatId) {
        return jpa.findByChatId(chatId).map(mapper::toDomain);
    }

    @Override
    public Optional<User> readByUUID(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<User> save(CreateUserDto dto) {
        return jpa.findByChatId(dto.chatId()).map(mapper::toDomain).or(() -> {
            UserJpaEntity entity = new UserJpaEntity(UUID.randomUUID(), dto.chatId(), true);
            return Optional.of(mapper.toDomain(jpa.save(entity)));
        });
    }

    @Override
    @Transactional
    public Optional<User> delete(UUID uuid) {
        return jpa.findById(uuid).map(entity -> {
            jpa.delete(entity);
            return mapper.toDomain(entity);
        });
    }
}
