package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.domain.bot.CommandInterface;
import backend.academy.linktracker.bot.properties.TelegramProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class TelegramConfiguration {

    @Bean
    public TelegramBot telegramBot(TelegramProperties properties) {
        var builder = new TelegramBot.Builder(properties.getToken())
                .apiUrl(properties.getUrl())
                .updateListenerSleep(properties.getUpdateListenerSleep().toMillis());

        if (properties.isDebug()) {
            builder.debug();
        }

        return builder.build();
    }

    @Bean
    @Profile("!test")
    public CommandLineRunner menuInitializer(TelegramBot bot, List<CommandInterface> commands) {
        return args -> {
            try {
                log.info("Настройка меню команд Telegram...");

                BotCommand[] botCommands = commands.stream()
                        .filter(cmd -> cmd.getMenuName() != null)
                        .map(cmd -> new BotCommand(cmd.getMenuName(), cmd.getMenuDescription()))
                        .toArray(BotCommand[]::new);

                if (botCommands.length > 0) {
                    SetMyCommands request = new SetMyCommands(botCommands);
                    BaseResponse response = bot.execute(request);
                    if (response.isOk()) {
                        log.info("Меню команд успешно обновлено");
                    } else {
                        log.error("Ошибка обновления меню: {}", response.description());
                    }
                }
            } catch (Exception e) {
                log.error(
                        "Бот запущен без доступа к Telegram API. Он начнет работу, когда появится связь. Ошибка: {}",
                        e.getMessage());
            }
        };
    }
}
