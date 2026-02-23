package backend.academy.linktracker.bot.test.bot;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.bot.commands.HelpCommand;
import backend.academy.linktracker.bot.application.bot.commands.StartCommand;
import backend.academy.linktracker.bot.application.bot.commands.UnknownCommand;
import backend.academy.linktracker.bot.application.users.usecases.CreateUserUseCase;
import backend.academy.linktracker.bot.domain.users.entities.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

@ExtendWith(MockitoExtension.class)
class CommandsTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private CreateUserUseCase createUserUseCase;

    private StartCommand startCommand;
    private HelpCommand helpCommand;
    private UnknownCommand unknownCommand;

    @BeforeEach
    void setUp() {
        startCommand = new StartCommand(createUserUseCase, messageSource);
        helpCommand = new HelpCommand(messageSource);
        unknownCommand = new UnknownCommand(messageSource);
    }

    @Test
    void StartCommandTest() {
        Long chatId = 123L;
        String expectedResponse = "True";

        when(createUserUseCase.execute(any())).thenReturn(Optional.of(new User()));

        when(messageSource.getMessage(eq("bot.command.start.message"), any(), any()))
                .thenReturn(expectedResponse);

        String result = startCommand.execute(chatId, "/start");

        assertThat(result).isEqualTo(expectedResponse);
        verify(createUserUseCase).execute(any());
    }

    @Test
    void StartCommandErrorTest() {
        Long chatId = 123L;
        String expectedResponse = "True";

        when(createUserUseCase.execute(any())).thenThrow(new RuntimeException());

        when(messageSource.getMessage(eq("bot.error"), any(), any())).thenReturn(expectedResponse);

        String result = startCommand.execute(chatId, "/start");

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void HelpCommandTest() {
        String expectedResponse = "True";
        when(messageSource.getMessage(eq("bot.command.help.message"), any(), any()))
                .thenReturn(expectedResponse);

        String result = helpCommand.execute(123L, "/help");

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void UnknownCommandTest() {
        String expectedResponse = "True";
        when(messageSource.getMessage(eq("bot.command.unknown.message"), any(), any()))
                .thenReturn(expectedResponse);

        String result = unknownCommand.execute(123L, "abracadabra");

        assertThat(result).isEqualTo(expectedResponse);
    }
}
