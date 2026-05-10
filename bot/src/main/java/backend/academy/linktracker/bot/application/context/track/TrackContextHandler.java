package backend.academy.linktracker.bot.application.context.track;

import backend.academy.linktracker.bot.application.context.track.usecases.DeleteTrackContextUseCase;
import backend.academy.linktracker.bot.application.context.track.usecases.ReadTrackContextUseCase;
import backend.academy.linktracker.bot.application.context.track.usecases.SaveTrackContextUseCase;
import backend.academy.linktracker.bot.domain.api.ScrapperClient;
import backend.academy.linktracker.bot.domain.api.dtos.AddLinkRequest;
import backend.academy.linktracker.bot.domain.context.ContextHandler;
import backend.academy.linktracker.bot.domain.context.ContextResult;
import backend.academy.linktracker.bot.domain.context.ContextType;
import backend.academy.linktracker.bot.domain.context.track.TagsParsingUtil;
import backend.academy.linktracker.bot.domain.context.track.TrackContext;
import backend.academy.linktracker.bot.domain.context.track.TrackStep;
import java.net.URI;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RequiredArgsConstructor
public class TrackContextHandler implements ContextHandler {

    private final MessageSource messageSource;
    private final ReadTrackContextUseCase readTrackUseCase;
    private final SaveTrackContextUseCase saveTrackUseCase;
    private final DeleteTrackContextUseCase deleteTrackUseCase;
    private final ScrapperClient scrapperClient;

    @Override
    public ContextType getSupportedType() {
        return ContextType.TRACK;
    }

    @Override
    public ContextResult handle(Long chatId, String text) {
        return readTrackUseCase
                .execute(chatId)
                .map(context -> process(context, text))
                .orElseGet(ContextResult::reject);
    }

    private ContextResult process(TrackContext context, String text) {
        if (!isValid(context.getStep(), text)) {
            deleteTrackUseCase.execute(context.getChatId());
            return ContextResult.reject();
        }

        fillData(context, text);
        context.nextStep();

        if (context.isFinished()) {
            return finishProcess(context);
        }

        saveTrackUseCase.execute(context);
        return ContextResult.success(getQuestion(context.getStep()), false);
    }

    private boolean isValid(TrackStep step, String text) {
        return switch (step) {
            case TrackStep.AWAITING_LINK -> text.startsWith("http");
            case TrackStep.AWAITING_TAGS -> !text.startsWith("/");
            default -> false;
        };
    }

    private void fillData(TrackContext context, String text) {
        if (context.getStep() == TrackStep.AWAITING_LINK) {
            context.setLink(text);
        } else if (context.getStep() == TrackStep.AWAITING_TAGS) {
            context.setTags(TagsParsingUtil.parseTags(text));
        }
    }

    private ContextResult finishProcess(TrackContext context) {
        AddLinkRequest req = new AddLinkRequest(URI.create(context.getLink()), context.getTags());

        String ans = "";
        try {
            scrapperClient.addLink(context.getChatId(), req);
            String tagsString = String.join(", ", context.getTags());
            ans = messageSource.getMessage(
                    "bot.command.track.add", new Object[] {context.getLink(), tagsString}, Locale.of("ru"));

        } catch (HttpClientErrorException.Conflict e) {
            ans = messageSource.getMessage(
                    "bot.error.link_already_tracked", new Object[] {context.getLink()}, Locale.of("ru"));

        } catch (HttpClientErrorException.NotFound e) {
            ans = messageSource.getMessage("bot.error.user_not_found", null, Locale.of("ru"));

        } catch (Exception e) {
            ans = messageSource.getMessage("bot.error", null, Locale.of("ru"));

        } finally {
            deleteTrackUseCase.execute(context.getChatId());
        }
        return ContextResult.success(ans, true);
    }

    private String getQuestion(TrackStep step) {
        return step == TrackStep.AWAITING_TAGS ? "Введите теги через запятую:" : "Ошибка";
    }
}
