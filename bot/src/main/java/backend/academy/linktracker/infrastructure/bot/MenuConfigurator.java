package backend.academy.linktracker.infrastructure.bot;

import backend.academy.linktracker.domain.bot.CommandInterface;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuConfigurator {

    private final TelegramBot bot;
    private final List<CommandInterface> commands;

    @PostConstruct
    public void registerMenuCommands() {
        com.pengrad.telegrambot.model.BotCommand[] botCommands = commands.stream()
                .filter(cmd -> cmd.getMenuName() != null)
                .map(cmd -> new com.pengrad.telegrambot.model.BotCommand(cmd.getMenuName(), cmd.getMenuDescription()))
                .toArray(com.pengrad.telegrambot.model.BotCommand[]::new);

        SetMyCommands request = new SetMyCommands(botCommands);
        BaseResponse response = bot.execute(request);

        if (response.isOk()) {
            log.info("Меню команд успешно обновлено");
        } else {
            log.error("Ошибка обновления меню: {}", response.description());
        }
    }
}
