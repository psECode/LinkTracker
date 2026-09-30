package backend.academy.linktracker.scrapper.infrastructure.api.errors;

import java.io.Serial;

public class SubscriptionNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 7403208119229037277L;

    public SubscriptionNotFoundException(String url) {
        super("Подписка на ссылку не найдена: " + url);
    }
}
