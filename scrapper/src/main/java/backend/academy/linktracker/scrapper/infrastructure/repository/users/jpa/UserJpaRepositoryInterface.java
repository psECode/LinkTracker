package backend.academy.linktracker.scrapper.infrastructure.repository.users.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepositoryInterface extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByTelegramId(Long telegramId);

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByTelegramId(Long telegramId);

    boolean existsByEmail(String email);
}
