package backend.academy.linktracker.scrapper.infrastructure.api.errors;

import java.io.Serial;

public class UserAlreadyExistsException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 602172615340506957L;

    public UserAlreadyExistsException(String email) {
        super("Пользователь с email " + email + " уже зарегистрирован");
    }
}
