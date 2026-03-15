package backend.academy.linktracker.scrapper.infrastructure.api.errors;

import java.io.Serial;

public class SubscriptionNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 7403208119229037277L;

    public SubscriptionNotFoundException(Long chatId, String url) {
        super("Чат " + chatId + " не подписан на ссылку: " + url);
    }
}
