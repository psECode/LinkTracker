package backend.academy.linktracker.bot.test.bot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.bot.CommandDispatcher;
import backend.academy.linktracker.bot.application.bot.commands.HelpCommand;
import backend.academy.linktracker.bot.application.bot.commands.StartCommand;
import backend.academy.linktracker.bot.application.bot.commands.UnknownCommand;
import backend.academy.linktracker.bot.application.users.usecases.CreateUserUseCase;
import backend.academy.linktracker.bot.domain.bot.CommandInterface;
import backend.academy.linktracker.bot.domain.bot.MessageSenderService;
import backend.academy.linktracker.bot.domain.users.entities.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

@ExtendWith(MockitoExtension.class)
class DispatcherTest {

    @Mock
    private MessageSenderService messageSenderService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private CreateUserUseCase createUserUseCase;

    private CommandDispatcher dispatcher;
    private final Long chatId = 12345L;

    @BeforeEach
    void setUp() {
        List<CommandInterface> commands = List.of(
                new StartCommand(createUserUseCase, messageSource),
                new HelpCommand(messageSource),
                new UnknownCommand(messageSource));

        dispatcher = new CommandDispatcher(commands, messageSenderService);
    }

    @Test
    void DispatchStartTest() {
        when(createUserUseCase.execute(any())).thenReturn(Optional.of(new User()));
        givenMessage("bot.command.start.message");

        dispatcher.dispatch(chatId, "/start");

        thenMessageShouldBeSent();
    }

    @Test
    void DispatchHelpTest() {
        givenMessage("bot.command.help.message");

        dispatcher.dispatch(chatId, "/help");

        thenMessageShouldBeSent();
    }

    @Test
    void DispatchUnknownTest() {
        givenMessage("bot.command.unknown.message");

        dispatcher.dispatch(chatId, "abracadabra");

        thenMessageShouldBeSent();
    }

    private void givenMessage(String key) {
        when(messageSource.getMessage(eq(key), any(), any())).thenReturn("True");
    }

    private void thenMessageShouldBeSent() {
        verify(messageSenderService).sendText(chatId, "True");
    }
}
