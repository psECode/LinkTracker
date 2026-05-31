package backend.academy.linktracker.bot.test.bot;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.api.ScrapperServiceInterface;
import backend.academy.linktracker.bot.application.bot.commands.HelpCommand;
import backend.academy.linktracker.bot.application.bot.commands.StartCommand;
import backend.academy.linktracker.bot.application.bot.commands.TrackCommand;
import backend.academy.linktracker.bot.application.bot.commands.UnknownCommand;
import backend.academy.linktracker.bot.application.bot.commands.UntrackCommand;
import backend.academy.linktracker.bot.application.context.track.usecases.SaveTrackContextUseCase;
import backend.academy.linktracker.bot.application.context.untrack.usecases.SaveUntrackContextUseCase;
import backend.academy.linktracker.bot.application.context.usecases.SetActiveContextUseCase;
import backend.academy.linktracker.bot.domain.api.dtos.AddLinkRequest;
import backend.academy.linktracker.bot.domain.api.dtos.LinkResponse;
import backend.academy.linktracker.bot.domain.api.dtos.ListLinksResponse;
import backend.academy.linktracker.bot.domain.context.ContextType;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@Import(com.fasterxml.jackson.databind.ObjectMapper.class)
class CommandsTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private SetActiveContextUseCase setActiveContext;

    @Mock
    private SaveTrackContextUseCase saveTrackContext;

    @Mock
    private ScrapperServiceInterface scrapperClient;

    @Mock
    private SaveUntrackContextUseCase saveUntrackContext;

    private StartCommand startCommand;
    private HelpCommand helpCommand;
    private TrackCommand trackCommand;
    private UnknownCommand unknownCommand;
    private UntrackCommand untrackCommand;
    private ObjectMapper mapper;

    private final Long chatId = 123L;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        startCommand = new StartCommand(messageSource, scrapperClient);
        helpCommand = new HelpCommand(messageSource);
        unknownCommand = new UnknownCommand(messageSource);
        trackCommand = new TrackCommand(setActiveContext, saveTrackContext, messageSource, scrapperClient);
        untrackCommand =
                new UntrackCommand(scrapperClient, setActiveContext, saveUntrackContext, messageSource, objectMapper);
    }

    @Test
    void startCommand() {
        givenMessage("bot.command.start.message", "Привет!");

        String result = startCommand.execute(chatId, "/start");

        assertThat(result).isEqualTo("Привет!");
    }

    @Test
    void trackContextStart() {
        givenMessage("bot.command.track.start", "Пришлите ссылку");

        String result = trackCommand.execute(chatId, "/track");

        assertThat(result).isEqualTo("Пришлите ссылку");
        verify(setActiveContext).execute(chatId, ContextType.TRACK);
        verify(saveTrackContext).execute(any());
    }

    @Test
    void immediateTrackHappy() {
        String url = "https://github.com";
        String text = "/track " + url + " tag1,tag2";
        givenMessage("bot.command.track.add", "true");

        String result = trackCommand.execute(chatId, text);

        assertThat(result).isEqualTo("true");
        verify(scrapperClient).addLink(eq(chatId), eq(new AddLinkRequest(URI.create(url), List.of("tag1", "tag2"))));
    }

    @Test
    void immediateTrackConflict() {
        String url = "https://github.com";
        String text = "/track " + url;

        when(messageSource.getMessage(eq("bot.error.link_already_tracked"), any(), any()))
                .thenReturn("уже отслеживаете");
        doThrow(createHttpException(HttpStatus.CONFLICT, null))
                .when(scrapperClient)
                .addLink(eq(chatId), any());

        String result = trackCommand.execute(chatId, text);

        assertThat(result).contains("уже отслеживаете");
    }

    @Test
    void immediateTrackNonExistentUser() {
        String text = "/track https://github.com";
        givenMessage("bot.error.user_not_found", "true");

        doThrow(createHttpException(HttpStatus.NOT_FOUND, null))
                .when(scrapperClient)
                .addLink(eq(chatId), any());

        String result = trackCommand.execute(chatId, text);

        assertThat(result).isEqualTo("true");
    }

    @Test
    void immediateTrackRandomError() {
        String text = "/track https://github.com";
        givenMessage("bot.error", "true");

        doThrow(new RuntimeException()).when(scrapperClient).addLink(any(), any());

        String result = trackCommand.execute(chatId, text);

        assertThat(result).isEqualTo("true");
    }

    @Test
    void startUntrackContext() {
        var link = new LinkResponse(1L, URI.create("http://gh.com"), List.of());
        when(scrapperClient.getAllLinks(chatId)).thenReturn(new ListLinksResponse(List.of(link), 1));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Response");

        String result = untrackCommand.execute(chatId, "/untrack");

        assertThat(result).isNotNull();
        verify(setActiveContext).execute(chatId, ContextType.UNTRACK);
        verify(saveUntrackContext).execute(any());
    }

    @Test
    void immediateUntrackHappy() {
        String url = "https://github.com/user/repo";
        String text = "/untrack " + url;
        when(messageSource.getMessage(eq("bot.command.untrack.success"), any(), any()))
                .thenReturn("Deleted");

        String result = untrackCommand.execute(chatId, text);

        assertThat(result).isEqualTo("Deleted");
        verify(scrapperClient).removeLink(eq(chatId), any());
    }

    @Test
    void immediateNonExistentUserUntrack() {
        String url = "https://github.com/user/repo";
        var exception = createHttpException(HttpStatus.NOT_FOUND, "UserNotFoundException");

        doThrow(exception).when(scrapperClient).removeLink(eq(chatId), any());
        when(messageSource.getMessage(eq("bot.error.user_not_found"), any(), any()))
                .thenReturn("No User");

        String result = untrackCommand.execute(chatId, "/untrack " + url);

        assertThat(result).isEqualTo("No User");
    }

    @Test
    void immediateNonExistentLinkUntrack() {
        String url = "https://github.com/user/repo";
        var exception = createHttpException(HttpStatus.NOT_FOUND, "LinkNotFoundException");

        doThrow(exception).when(scrapperClient).removeLink(eq(chatId), any());
        when(messageSource.getMessage(eq("bot.error.link_not_found"), any(), any()))
                .thenReturn("No Link");

        String result = untrackCommand.execute(chatId, "/untrack " + url);

        assertThat(result).isEqualTo("No Link");
    }

    @Test
    void helpCommand() {
        givenMessage("bot.command.help.message", "true");
        assertThat(helpCommand.execute(chatId, "/help")).isEqualTo("true");
    }

    @Test
    void unknownCommand() {
        givenMessage("bot.command.unknown.message", "true");
        assertThat(unknownCommand.execute(chatId, "abracadabra")).isEqualTo("true");
    }

    private void givenMessage(String key, String response) {
        lenient().when(messageSource.getMessage(eq(key), any(), any())).thenReturn(response);
    }

    private HttpClientErrorException createHttpException(HttpStatus status, String body) {
        byte[] json = null;
        if (body != null) {
            json = String.format("{\"exceptionName\": \"%s\"}", body).getBytes();
        }
        return HttpClientErrorException.create(
                status, status.getReasonPhrase(), org.springframework.http.HttpHeaders.EMPTY, json, null);
    }
}
