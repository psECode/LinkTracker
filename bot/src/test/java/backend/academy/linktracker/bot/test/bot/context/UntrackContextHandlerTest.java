package backend.academy.linktracker.bot.test.bot.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.context.untrack.UntrackContextHandler;
import backend.academy.linktracker.bot.application.context.untrack.usecases.DeleteUntrackContextUseCase;
import backend.academy.linktracker.bot.domain.api.ScrapperClient;
import backend.academy.linktracker.bot.domain.context.ContextResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UntrackContextHandlerTest {

    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private DeleteUntrackContextUseCase deleteUntrackContextUseCase;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private UntrackContextHandler handler;

    private final Long chatId = 12345L;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        lenient().when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Response Message");
        handler = new UntrackContextHandler(messageSource, scrapperClient, deleteUntrackContextUseCase, objectMapper);
    }

    @Test
    void happyTest() {
        String link = "https://github.com/user/repo";
        when(messageSource.getMessage(eq("bot.command.untrack.success"), any(), any()))
                .thenReturn("Удалено");

        ContextResult result = handler.handle(chatId, link);

        assertThat(result.handled()).isTrue();
        assertThat(result.isFinished()).isTrue();
        assertThat(result.message()).isEqualTo("Удалено");

        verify(scrapperClient).removeLink(eq(chatId), any());
        verify(deleteUntrackContextUseCase).execute(chatId);
    }

    @Test
    void nonExistentUserTest() {
        String link = "https://github.com/user/repo";
        var exception = createHttpException(HttpStatus.NOT_FOUND, "UserNotFoundException");

        doThrow(exception).when(scrapperClient).removeLink(any(), any());
        when(messageSource.getMessage(eq("bot.error.user_not_found"), any(), any()))
                .thenReturn("No User");

        ContextResult result = handler.handle(chatId, link);

        assertThat(result.message()).isEqualTo("No User");
        verify(deleteUntrackContextUseCase).execute(chatId);
    }

    @Test
    void nonExistentLinkTest() {
        String link = "https://github.com/user/repo";
        var exception = createHttpException(HttpStatus.NOT_FOUND, "LinkNotFoundException");

        doThrow(exception).when(scrapperClient).removeLink(any(), any());
        when(messageSource.getMessage(eq("bot.error.link_not_found"), any(), any()))
                .thenReturn("No Link");

        ContextResult result = handler.handle(chatId, link);

        assertThat(result.message()).isEqualTo("No Link");
        verify(deleteUntrackContextUseCase).execute(chatId);
    }

    @Test
    void randomErrorTest() {
        doThrow(new RuntimeException()).when(scrapperClient).removeLink(any(), any());
        when(messageSource.getMessage(eq("bot.error"), any(), any())).thenReturn("Error");

        ContextResult result = handler.handle(chatId, "https://link.com");

        assertThat(result.message()).isEqualTo("Error");
        verify(deleteUntrackContextUseCase).execute(chatId);
    }

    @Test
    void handle_CommandInterrupt() {
        ContextResult result = handler.handle(chatId, "/help");

        assertThat(result.handled()).isFalse();
        verifyNoInteractions(scrapperClient);
        verifyNoInteractions(deleteUntrackContextUseCase);
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
