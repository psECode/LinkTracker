package backend.academy.linktracker.scrapper.infrastructure.repository.users.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepositoryInterface extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByChatId(Long chatId);

    boolean existsByChatId(Long chatId);
}
