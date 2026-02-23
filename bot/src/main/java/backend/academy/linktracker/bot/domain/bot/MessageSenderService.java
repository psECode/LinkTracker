package backend.academy.linktracker.bot.domain.bot;

public interface MessageSenderService {
    void sendText(Long chatId, String text);
}
