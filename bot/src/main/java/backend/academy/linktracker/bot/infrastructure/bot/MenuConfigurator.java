package backend.academy.linktracker.bot.infrastructure.bot;

import backend.academy.linktracker.bot.domain.bot.CommandInterface;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
public class MenuConfigurator {

    private final TelegramBot bot;
    private final List<CommandInterface> commands;

    @PostConstruct
    public void registerMenuCommands() {
        BotCommand[] botCommands = commands.stream()
                .filter(cmd -> cmd.getMenuName() != null)
                .map(cmd -> new BotCommand(cmd.getMenuName(), cmd.getMenuDescription()))
                .toArray(BotCommand[]::new);

        SetMyCommands request = new SetMyCommands(botCommands);
        BaseResponse response = bot.execute(request);

        if (response.isOk()) {
            log.info("Меню команд успешно обновлено");
        } else {
            log.error("Ошибка обновления меню: {}", response.description());
        }
    }
}
