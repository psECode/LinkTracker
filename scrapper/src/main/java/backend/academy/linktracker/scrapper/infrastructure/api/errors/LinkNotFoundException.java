package backend.academy.linktracker.scrapper.infrastructure.api.errors;

import java.io.Serial;

public class LinkNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5598541224768535302L;

    public LinkNotFoundException(String url) {
        super("Ссылка не найдена: " + url);
    }
}
