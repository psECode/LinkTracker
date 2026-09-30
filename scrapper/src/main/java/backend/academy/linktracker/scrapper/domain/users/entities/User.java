package backend.academy.linktracker.scrapper.domain.users.entities;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class User {
    private final UUID id;

    private final Long telegramId;

    private final String email;

    private final String passwordHash;

    private Boolean isActive;
}
