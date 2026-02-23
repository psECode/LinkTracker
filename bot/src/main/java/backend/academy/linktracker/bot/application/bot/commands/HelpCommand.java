package backend.academy.linktracker.bot.application.bot.commands;

import backend.academy.linktracker.bot.domain.bot.CommandInterface;
import backend.academy.linktracker.bot.domain.bot.CommandType;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HelpCommand implements CommandInterface {
    private final MessageSource messageSource;

    @Override
    public String getMenuName() {
        return "help";
    }

    @Override
    public String getMenuDescription() {
        return "Вывести информацию о боте";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.HELP;
    }

    @Override
    public String execute(Long chatId, String text) {
        return messageSource.getMessage("bot.command.help.message", null, Locale.of("ru"));
    }
}
