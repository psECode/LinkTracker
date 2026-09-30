package backend.academy.linktracker.scrapper.infrastructure.api.errors;

import java.io.Serial;
import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 602172615340506956L;

    public UserNotFoundException(Long telegramId) {
        super("Пользователь с Telegram-ID " + telegramId + " не зарегистрирован");
    }

    public UserNotFoundException(UUID userId) {
        super("Пользователь " + userId + " не найден");
    }
}
