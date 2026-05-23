package backend.academy.linktracker.scrapper.infrastructure.repository.users.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity {
    @Id
    private UUID id;

    @Column(name = "telegram_id", unique = true, nullable = false)
    private Long chatId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
