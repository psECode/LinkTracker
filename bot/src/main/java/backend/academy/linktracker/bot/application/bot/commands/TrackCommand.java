package backend.academy.linktracker.bot.application.bot.commands;

import backend.academy.linktracker.bot.application.api.ScrapperServiceInterface;
import backend.academy.linktracker.bot.application.context.track.usecases.SaveTrackContextUseCase;
import backend.academy.linktracker.bot.application.context.usecases.SetActiveContextUseCase;
import backend.academy.linktracker.bot.domain.api.dtos.AddLinkRequest;
import backend.academy.linktracker.bot.domain.bot.CommandInterface;
import backend.academy.linktracker.bot.domain.bot.CommandType;
import backend.academy.linktracker.bot.domain.context.ContextType;
import backend.academy.linktracker.bot.domain.context.track.TagsParsingUtil;
import backend.academy.linktracker.bot.domain.context.track.TrackContext;
import backend.academy.linktracker.bot.domain.context.track.TrackStep;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RequiredArgsConstructor
public class TrackCommand implements CommandInterface {

    private final SetActiveContextUseCase setActiveContext;
    private final SaveTrackContextUseCase saveTrackContext;
    private final MessageSource messageSource;
    private final ScrapperServiceInterface scrapperClient;

    @Override
    public String execute(Long chatId, String text) {
        String[] parts = text.trim().split("\\s+", 3);

        if (parts.length == 1) {
            setActiveContext.execute(chatId, ContextType.TRACK);
            saveTrackContext.execute(TrackContext.builder()
                    .chatId(chatId)
                    .step(TrackStep.AWAITING_LINK)
                    .build());
            return messageSource.getMessage("bot.command.track.start", null, Locale.of("ru"));
        }

        return executeImmediately(parts, chatId);
    }

    public String executeImmediately(String[] parts, Long chatId) {
        for (String part : parts) {
            System.out.println(part + " ");
        }
        String link = parts[1];
        List<String> tags = (parts.length > 2) ? TagsParsingUtil.parseTags(parts[2]) : List.of();

        AddLinkRequest req = new AddLinkRequest(URI.create(link), tags);
        try {
            scrapperClient.addLink(chatId, req);
            String tagsString = String.join(", ", tags);
            return messageSource.getMessage("bot.command.track.add", new Object[] {link, tagsString}, Locale.of("ru"));
        } catch (HttpClientErrorException.Conflict e) {
            return messageSource.getMessage("bot.error.link_already_tracked", new Object[] {link}, Locale.of("ru"));

        } catch (HttpClientErrorException.NotFound e) {
            return messageSource.getMessage("bot.error.user_not_found", null, Locale.of("ru"));

        } catch (Exception e) {
            return messageSource.getMessage("bot.error", null, Locale.of("ru"));
        }
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.TRACK;
    }

    @Override
    public String getMenuName() {
        return "track";
    }

    @Override
    public String getMenuDescription() {
        return messageSource.getMessage("bot.command.track.description", null, Locale.of("ru"));
    }
}
