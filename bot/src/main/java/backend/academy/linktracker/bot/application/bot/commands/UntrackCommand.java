package backend.academy.linktracker.bot.application.bot.commands;

import backend.academy.linktracker.bot.application.context.LinkListFormattingUtil;
import backend.academy.linktracker.bot.application.context.untrack.usecases.SaveUntrackContextUseCase;
import backend.academy.linktracker.bot.application.context.usecases.SetActiveContextUseCase;
import backend.academy.linktracker.bot.domain.api.ScrapperClient;
import backend.academy.linktracker.bot.domain.api.dtos.ListLinksResponse;
import backend.academy.linktracker.bot.domain.api.dtos.RemoveLinkRequest;
import backend.academy.linktracker.bot.domain.bot.CommandInterface;
import backend.academy.linktracker.bot.domain.bot.CommandType;
import backend.academy.linktracker.bot.domain.context.ContextType;
import backend.academy.linktracker.bot.domain.context.untrack.UntrackContext;
import backend.academy.linktracker.bot.domain.context.untrack.UntrackStep;
import backend.academy.linktracker.bot.infrastructure.api.dtos.ApiErrorResponse;
import java.net.URI;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Import(com.fasterxml.jackson.databind.ObjectMapper.class)
public class UntrackCommand implements CommandInterface {

    private final ScrapperClient scrapperClient;
    private final SetActiveContextUseCase setActiveContext;
    private final SaveUntrackContextUseCase saveUntrackContext;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(Long chatId, String text) {
        String[] parts = text.trim().split("\\s+", 2);
        System.out.println("here");
        try {
            if (parts.length == 1) {
                return startUntrackContext(chatId);
            }

            return executeImmediately(chatId, parts[1]);
        } catch (HttpClientErrorException.NotFound e) {
            ApiErrorResponse errorBody = parseError(e);

            if (errorBody != null && "UserNotFoundException".equals(errorBody.exceptionName())) {
                return messageSource.getMessage("bot.error.user_not_found", null, Locale.of("ru"));
            }

            return messageSource.getMessage("bot.error.link_not_found", null, Locale.of("ru"));

        } catch (Exception e) {
            return messageSource.getMessage("bot.error", null, Locale.of("ru"));
        }
    }

    private String startUntrackContext(Long chatId) {

        ListLinksResponse response = scrapperClient.getAllLinks(chatId);
        if (response.links().isEmpty()) {
            return messageSource.getMessage("bot.command.list.empty", null, Locale.of("ru"));
        }

        setActiveContext.execute(chatId, ContextType.UNTRACK);
        UntrackContext context = UntrackContext.builder()
                .chatId(chatId)
                .step(UntrackStep.WAITING_FOR_LINK)
                .link(null)
                .build();
        saveUntrackContext.execute(context);

        String listText = LinkListFormattingUtil.execute(response.links());
        return listText + "\n\n" + messageSource.getMessage("bot.command.untrack.start", null, Locale.of("ru"));
    }

    private String executeImmediately(Long chatId, String link) {
        scrapperClient.removeLink(chatId, new RemoveLinkRequest(URI.create(link)));

        return messageSource.getMessage("bot.command.untrack.success", new Object[] {link}, Locale.of("ru"));
    }

    private ApiErrorResponse parseError(HttpClientErrorException e) {
        try {
            return objectMapper.readValue(e.getResponseBodyAsByteArray(), ApiErrorResponse.class);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.UNTRACK;
    }

    @Override
    public String getMenuName() {
        return "untrack";
    }

    @Override
    public String getMenuDescription() {
        return "Прекратить отслеживание ссылки";
    }
}
