package backend.academy.linktracker.domain.bot;

public interface MessageSenderPort {
    void sendText(Long chatId, String text);
}
