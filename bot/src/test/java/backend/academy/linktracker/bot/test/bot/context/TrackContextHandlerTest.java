package backend.academy.linktracker.bot.test.bot.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.api.ScrapperServiceInterface;
import backend.academy.linktracker.bot.application.context.track.TrackContextHandler;
import backend.academy.linktracker.bot.application.context.track.usecases.DeleteTrackContextUseCase;
import backend.academy.linktracker.bot.application.context.track.usecases.ReadTrackContextUseCase;
import backend.academy.linktracker.bot.application.context.track.usecases.SaveTrackContextUseCase;
import backend.academy.linktracker.bot.domain.api.dtos.AddLinkRequest;
import backend.academy.linktracker.bot.domain.context.ContextResult;
import backend.academy.linktracker.bot.domain.context.track.TrackContext;
import backend.academy.linktracker.bot.domain.context.track.TrackStep;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class TrackContextHandlerTest {

    @Mock
    private ReadTrackContextUseCase readTrackUseCase;

    @Mock
    private SaveTrackContextUseCase saveTrackUseCase;

    @Mock
    private DeleteTrackContextUseCase deleteTrackUseCase;

    @Mock
    private ScrapperServiceInterface scrapperClient;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private TrackContextHandler handler;

    private final Long chatId = 123L;
    private TrackContext context;

    @BeforeEach
    void setUp() {
        context = TrackContext.builder()
                .chatId(chatId)
                .step(TrackStep.AWAITING_TAGS)
                .link("https://github.com/user/repo")
                .tags(List.of("tag1"))
                .build();
    }

    @Test
    void completionSuccessTest() {
        when(readTrackUseCase.execute(chatId)).thenReturn(Optional.of(context));
        givenMessage("bot.command.track.add", "Успех");

        ContextResult result = handler.handle(chatId, "tag1");

        assertThat(result.isFinished()).isTrue();
        assertThat(result.message()).isEqualTo("Успех");

        verify(deleteTrackUseCase, atLeastOnce()).execute(chatId);
        verify(scrapperClient, atLeastOnce()).addLink(eq(chatId), any(AddLinkRequest.class));
    }

    @Test
    void completionConflictTest() {
        when(readTrackUseCase.execute(chatId)).thenReturn(Optional.of(context));

        doThrow(createHttpException(HttpStatus.CONFLICT)).when(scrapperClient).addLink(eq(chatId), any());

        givenMessage("bot.error.link_already_tracked", "Уже есть");

        ContextResult result = handler.handle(chatId, "tag1");

        assertThat(result.message()).isEqualTo("Уже есть");
        verify(deleteTrackUseCase, atLeastOnce()).execute(chatId);
    }

    @Test
    void completionNotFoundTest() {
        when(readTrackUseCase.execute(chatId)).thenReturn(Optional.of(context));

        doThrow(createHttpException(HttpStatus.NOT_FOUND)).when(scrapperClient).addLink(eq(chatId), any());

        givenMessage("bot.error.user_not_found", "Нажми старт");

        ContextResult result = handler.handle(chatId, "tag1");

        assertThat(result.message()).isEqualTo("Нажми старт");
        verify(deleteTrackUseCase, atLeastOnce()).execute(chatId);
    }

    @Test
    void completionGenericErrorTest() {
        when(readTrackUseCase.execute(chatId)).thenReturn(Optional.of(context));

        doThrow(new RuntimeException("abracadabra")).when(scrapperClient).addLink(eq(chatId), any());

        givenMessage("bot.error", "Сбой");

        ContextResult result = handler.handle(chatId, "tag1");

        assertThat(result.message()).isEqualTo("Сбой");
        verify(deleteTrackUseCase, atLeastOnce()).execute(chatId);
    }

    private void givenMessage(String key, String response) {
        lenient().when(messageSource.getMessage(eq(key), any(), any())).thenReturn(response);
    }

    private HttpClientErrorException createHttpException(HttpStatus status) {
        return HttpClientErrorException.create(status, "Error", org.springframework.http.HttpHeaders.EMPTY, null, null);
    }
}
