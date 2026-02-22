package backend.academy.linktracker.infrastructure.bot;

import backend.academy.linktracker.domain.bot.MessageSenderPort;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramSenderAdapter implements MessageSenderPort {

    private final TelegramBot bot;

    @Override
    public void sendText(Long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        var response = bot.execute(request);

        if (!response.isOk()) {
            log.error("Ошибка отправки: {}", response.description());
        }
    }
}
