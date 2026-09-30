package backend.academy.linktracker.scrapper.infrastructure.repository.users.jpa;

import backend.academy.linktracker.scrapper.domain.users.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toDomain(UserJpaEntity entity) {
        return User.builder()
                .id(entity.getId())
                .telegramId(entity.getTelegramId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .isActive(entity.getIsActive())
                .build();
    }

    public UserJpaEntity toEntity(User domain) {
        return new UserJpaEntity(
                domain.getId(),
                domain.getTelegramId(),
                domain.getEmail(),
                domain.getPasswordHash(),
                domain.getIsActive());
    }
}
