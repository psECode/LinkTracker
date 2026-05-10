package backend.academy.linktracker.scrapper.infrastructure.api.errors;

import java.io.Serial;

public class UserNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 602172615340506956L;

    public UserNotFoundException(Long chatId) {
        super("Чат с ID " + chatId + " не зарегистрирован");
    }
}
