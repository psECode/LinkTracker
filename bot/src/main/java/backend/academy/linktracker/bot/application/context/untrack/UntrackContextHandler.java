package backend.academy.linktracker.bot.application.context.untrack;

import backend.academy.linktracker.bot.application.context.untrack.usecases.DeleteUntrackContextUseCase;
import backend.academy.linktracker.bot.domain.api.ScrapperClient;
import backend.academy.linktracker.bot.domain.api.dtos.RemoveLinkRequest;
import backend.academy.linktracker.bot.domain.context.ContextHandler;
import backend.academy.linktracker.bot.domain.context.ContextResult;
import backend.academy.linktracker.bot.domain.context.ContextType;
import backend.academy.linktracker.bot.infrastructure.api.dtos.ApiErrorResponse;
import java.net.URI;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class UntrackContextHandler implements ContextHandler {

    private final MessageSource messageSource;
    private final ScrapperClient scrapperClient;
    private final DeleteUntrackContextUseCase deleteUntrackData;
    private final ObjectMapper objectMapper;

    @Override
    public ContextType getSupportedType() {
        return ContextType.UNTRACK;
    }

    @Override
    public ContextResult handle(Long chatId, String text) {
        if (text.startsWith("/")) {
            return ContextResult.reject();
        }

        String ans = "";
        try {
            scrapperClient.removeLink(chatId, new RemoveLinkRequest(URI.create(text)));
            ans = messageSource.getMessage("bot.command.untrack.success", new Object[] {text}, Locale.of("ru"));

        } catch (HttpClientErrorException.NotFound e) {
            ApiErrorResponse errorBody = parseError(e);
            if (errorBody != null && "UserNotFoundException".equals(errorBody.exceptionName())) {
                ans = messageSource.getMessage("bot.error.user_not_found", null, Locale.of("ru"));
            } else {
                ans = messageSource.getMessage("bot.error.link_not_found", new Object[] {text}, Locale.of("ru"));
            }

        } catch (Exception e) {
            ans = messageSource.getMessage("bot.error", null, Locale.of("ru"));
        } finally {
            deleteUntrackData.execute(chatId);
        }
        return new ContextResult(true, ans, true);
    }

    private ApiErrorResponse parseError(HttpClientErrorException e) {
        try {
            return objectMapper.readValue(e.getResponseBodyAsByteArray(), ApiErrorResponse.class);
        } catch (Exception ex) {
            return null;
        }
    }
}
