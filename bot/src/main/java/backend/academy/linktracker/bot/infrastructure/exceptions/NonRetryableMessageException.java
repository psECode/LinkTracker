package backend.academy.linktracker.bot.infrastructure.exceptions;

import java.io.Serial;

public class NonRetryableMessageException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4661795698461626510L;

    public NonRetryableMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
