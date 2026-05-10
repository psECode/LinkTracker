package backend.academy.linktracker.bot.infrastructure.api.errors;

import java.io.Serial;

public class LinkAlreadyTrackedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5093927716792750090L;

    public LinkAlreadyTrackedException(String message) {
        super(message);
    }
}
