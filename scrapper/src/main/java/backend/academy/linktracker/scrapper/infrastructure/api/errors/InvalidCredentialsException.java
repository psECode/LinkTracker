package backend.academy.linktracker.scrapper.infrastructure.api.errors;

import java.io.Serial;

public class InvalidCredentialsException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 602172615340506958L;

    public InvalidCredentialsException() {
        super("Неверный email или пароль");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
