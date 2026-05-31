package backend.academy.linktracker.bot.test.bot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.api.ScrapperServiceInterface;
import backend.academy.linktracker.bot.application.bot.CommandDispatcher;
import backend.academy.linktracker.bot.application.bot.commands.HelpCommand;
import backend.academy.linktracker.bot.application.bot.commands.StartCommand;
import backend.academy.linktracker.bot.application.bot.commands.TrackCommand;
import backend.academy.linktracker.bot.application.bot.commands.UnknownCommand;
import backend.academy.linktracker.bot.application.context.track.usecases.SaveTrackContextUseCase;
import backend.academy.linktracker.bot.application.context.usecases.DeleteActiveContextUseCase;
import backend.academy.linktracker.bot.application.context.usecases.ReadActiveContextUseCase;
import backend.academy.linktracker.bot.application.context.usecases.SetActiveContextUseCase;
import backend.academy.linktracker.bot.domain.bot.CommandInterface;
import backend.academy.linktracker.bot.domain.bot.MessageSenderService;
import backend.academy.linktracker.bot.domain.context.ContextHandlerFactory;
import backend.academy.linktracker.bot.domain.context.ContextType;
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
    private ContextHandlerFactory flowHandlerFactory;

    @Mock
    private ReadActiveContextUseCase readActiveContext;

    @Mock
    private DeleteActiveContextUseCase deleteActiveContext;

    @Mock
    private SetActiveContextUseCase setActiveContext;

    @Mock
    private SaveTrackContextUseCase saveTrackContext;

    @Mock
    private ScrapperServiceInterface scrapperClient;

    private CommandDispatcher dispatcher;
    private final Long chatId = 12345L;

    @BeforeEach
    void setUp() {
        List<CommandInterface> commands = List.of(
                new StartCommand(messageSource, scrapperClient),
                new HelpCommand(messageSource),
                new TrackCommand(setActiveContext, saveTrackContext, messageSource, scrapperClient),
                new UnknownCommand(messageSource));

        dispatcher = new CommandDispatcher(
                commands, flowHandlerFactory, readActiveContext, deleteActiveContext, messageSenderService);
    }

    @Test
    void dispatchTrackCommand() {
        givenNoActiveContext();
        givenMessage("bot.command.track.start");

        dispatcher.dispatch(chatId, "/track");

        thenMessageShouldBeSent();
        verify(setActiveContext).execute(chatId, ContextType.TRACK);
    }

    @Test
    void dispatchHelp() {
        givenNoActiveContext();
        givenMessage("bot.command.help.message");

        dispatcher.dispatch(chatId, "/help");

        thenMessageShouldBeSent();
    }

    @Test
    void dispatchUnknown() {
        givenNoActiveContext();
        givenMessage("bot.command.unknown.message");

        dispatcher.dispatch(chatId, "какой-то текст");

        thenMessageShouldBeSent();
    }

    private void givenNoActiveContext() {
        when(readActiveContext.execute(chatId)).thenReturn(Optional.empty());
    }

    private void givenMessage(String key) {
        when(messageSource.getMessage(eq(key), any(), any())).thenReturn("True");
    }

    private void thenMessageShouldBeSent() {
        verify(messageSenderService).sendText(chatId, "True");
    }
}
