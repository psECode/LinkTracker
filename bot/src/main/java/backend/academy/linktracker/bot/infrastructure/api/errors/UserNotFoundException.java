package backend.academy.linktracker.bot.infrastructure.api.errors;

import java.io.Serial;

public class UserNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 2917963295192498824L;

    public UserNotFoundException(Long chatId) {
        super("Чат с ID " + chatId + " не зарегистрирован");
    }
}
