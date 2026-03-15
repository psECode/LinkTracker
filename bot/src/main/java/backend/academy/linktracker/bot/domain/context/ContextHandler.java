package backend.academy.linktracker.bot.domain.context;

public interface ContextHandler {
    ContextType getSupportedType();

    ContextResult handle(Long chatId, String text);
}
