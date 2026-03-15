package backend.academy.linktracker.scrapper.infrastructure.api.errors;

import java.io.Serial;

public class InvalidLinkException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -2612336969120747233L;

    public InvalidLinkException(String message) {
        super(message);
    }
}
