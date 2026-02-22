package backend.academy.linktracker.infrastructure.bot;

import backend.academy.linktracker.application.bot.CommandDispatcher;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramLongPollingService {

    private final TelegramBot bot;
    private final CommandDispatcher dispatcher;

    @PostConstruct
    public void start() {
        log.info("запускаем прием сообщений...");
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null && update.message().text() != null) {
                    Long chatId = update.message().chat().id();
                    String text = update.message().text();

                    dispatcher.dispatch(chatId, text);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
