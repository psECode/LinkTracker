package backend.academy.linktracker.bot.application.bot.usecases;

import backend.academy.linktracker.bot.domain.api.dtos.LinkUpdate;
import backend.academy.linktracker.bot.domain.bot.MessageSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProcessUpdateUseCase {
    private final MessageSenderService messageSender;

    public void execute(LinkUpdate update) {
        String messageText = String.format("Обновление: %s", update.description());

        for (Long chatId : update.tgChatIds()) {
            messageSender.sendText(chatId, messageText);
        }
    }
}
