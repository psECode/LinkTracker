package backend.academy.linktracker.bot.infrastructure.api.errors;

import java.io.Serial;

public class InvalidLinkException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -302807644654353898L;

    public InvalidLinkException(String message) {
        super(message);
    }
}
