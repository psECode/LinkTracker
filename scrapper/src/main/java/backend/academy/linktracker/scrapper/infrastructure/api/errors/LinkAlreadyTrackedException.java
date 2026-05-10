package backend.academy.linktracker.scrapper.infrastructure.api.errors;

import java.io.Serial;

public class LinkAlreadyTrackedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7312284529766810738L;

    public LinkAlreadyTrackedException(String message) {
        super(message);
    }
}
