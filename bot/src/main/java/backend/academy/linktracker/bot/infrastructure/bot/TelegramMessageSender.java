package backend.academy.linktracker.bot.infrastructure.bot;

import backend.academy.linktracker.bot.domain.bot.MessageSenderService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class TelegramMessageSender implements MessageSenderService {
    private final MeterRegistry meterRegistry;
    private final TelegramBot bot;

    @Override
    @CircuitBreaker(name = "telegramCB")
    @Retry(name = "telegramRetry")
    public void sendText(Long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text);
        var response = bot.execute(request);

        if (!response.isOk()) {
            log.error("Ошибка отправки: {}", response.description());
            throw new RuntimeException("Telegram API error: " + response.errorCode());
        }
        meterRegistry.counter("sent_notification_total").increment();
    }
}
