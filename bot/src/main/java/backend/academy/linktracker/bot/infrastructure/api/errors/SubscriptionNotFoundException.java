package backend.academy.linktracker.bot.infrastructure.api.errors;

import java.io.Serial;

public class SubscriptionNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -5933834317123614777L;

    public SubscriptionNotFoundException(Long chatId, String url) {
        super("Чат " + chatId + " не подписан на ссылку: " + url);
    }
}
