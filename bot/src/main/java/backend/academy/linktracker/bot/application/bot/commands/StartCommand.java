package backend.academy.linktracker.bot.application.bot.commands;

import backend.academy.linktracker.bot.application.users.usecases.CreateUserUseCase;
import backend.academy.linktracker.bot.domain.bot.CommandInterface;
import backend.academy.linktracker.bot.domain.bot.CommandType;
import backend.academy.linktracker.bot.domain.users.dtos.CreateUserDto;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartCommand implements CommandInterface {
    private final CreateUserUseCase createUserUseCase;
    private final MessageSource messageSource;

    @Override
    public String getMenuName() {
        return "start";
    }

    @Override
    public String getMenuDescription() {
        return messageSource.getMessage("bot.command.start.description", null, Locale.of("ru"));
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.START;
    }

    @Override
    public String execute(Long chatId, String text) {
        CreateUserDto dto = CreateUserDto.builder().chatId(chatId).build();
        try {
            createUserUseCase.execute(dto);
            return messageSource.getMessage("bot.command.start.message", null, Locale.of("ru"));
        } catch (Exception e) {
            return messageSource.getMessage("bot.error", null, Locale.of("ru"));
        }
    }
}
