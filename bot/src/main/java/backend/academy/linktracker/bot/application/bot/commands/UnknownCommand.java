package backend.academy.linktracker.bot.application.bot.commands;

import backend.academy.linktracker.bot.domain.bot.CommandInterface;
import backend.academy.linktracker.bot.domain.bot.CommandType;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnknownCommand implements CommandInterface {
    private final MessageSource messageSource;

    @Override
    public CommandType getCommandType() {
        return CommandType.UNKNOWN;
    }

    @Override
    public String execute(Long chatId, String text) {
        return messageSource.getMessage("bot.command.unknown.message", null, Locale.of("ru"));
    }
}
