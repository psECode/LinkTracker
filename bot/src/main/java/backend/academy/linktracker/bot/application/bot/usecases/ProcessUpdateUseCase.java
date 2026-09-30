package backend.academy.linktracker.bot.application.bot.usecases;

import backend.academy.linktracker.bot.domain.api.dtos.LinkUpdate;
import backend.academy.linktracker.bot.domain.bot.MessageSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessUpdateUseCase {
    private final MessageSenderService messageSender;

    public void execute(LinkUpdate update) {
        String messageText = String.format("Обновление: %s", update.description());
        int recipients = update.tgChatIds() == null ? 0 : update.tgChatIds().size();
        log.info("Доставка обновления id={} в Telegram: {} получателей", update.id(), recipients);

        for (Long chatId : update.tgChatIds()) {
            messageSender.sendText(chatId, messageText);
        }
    }
}