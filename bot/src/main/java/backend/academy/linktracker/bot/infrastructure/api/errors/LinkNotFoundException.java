package backend.academy.linktracker.bot.infrastructure.api.errors;

import java.io.Serial;

public class LinkNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 2171488696370679748L;

    public LinkNotFoundException(String url) {
        super("Ссылка не найдена: " + url);
    }
}
