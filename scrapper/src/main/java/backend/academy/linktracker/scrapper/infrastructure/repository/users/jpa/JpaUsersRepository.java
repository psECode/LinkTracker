package backend.academy.linktracker.scrapper.infrastructure.repository.users.jpa;

import backend.academy.linktracker.scrapper.domain.users.UsersRepository;
import backend.academy.linktracker.scrapper.domain.users.dtos.CreateWebUserDto;
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
    public Optional<User> readByTelegramId(Long telegramId) {
        return jpa.findByTelegramId(telegramId).map(mapper::toDomain);
    }

    @Override
    public Optional<User> readByEmail(String email) {
        return jpa.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> readByUUID(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<User> saveTelegramUser(Long telegramId) {
        return jpa.findByTelegramId(telegramId)
                .map(entity -> {
                    entity.setIsActive(true);
                    return mapper.toDomain(jpa.save(entity));
                })
                .or(() -> Optional.of(
                        mapper.toDomain(jpa.save(new UserJpaEntity(UUID.randomUUID(), telegramId, null, null, true)))));
    }

    @Override
    @Transactional
    public Optional<User> saveWebUser(CreateWebUserDto dto) {
        if (jpa.existsByEmail(dto.email())) {
            return Optional.empty();
        }
        UserJpaEntity entity = new UserJpaEntity(UUID.randomUUID(), null, dto.email(), dto.passwordHash(), true);
        return Optional.of(mapper.toDomain(jpa.save(entity)));
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
