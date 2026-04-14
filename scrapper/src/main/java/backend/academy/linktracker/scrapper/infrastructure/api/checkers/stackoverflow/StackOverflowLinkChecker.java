package backend.academy.linktracker.scrapper.infrastructure.api.checkers.stackoverflow;

import backend.academy.linktracker.scrapper.infrastructure.api.checkers.stackoverflow.entities.StackOverflowBaseResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.stackoverflow.entities.StackOverflowClient;
import backend.academy.linktracker.scrapper.domain.links.LinkType;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.LinkChecker;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.UpdateDescription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StackOverflowLinkChecker implements LinkChecker {
    private final StackOverflowClient stackOverflowClient;
    private static final String SITE = "stackoverflow";
    private static final String FILTER = "!6WPIOM79s79Y)";

    @Override
    public List<UpdateDescription> checkUpdates(Link link) {
        URI uri = URI.create(link.getUrl());
        String[] segments = uri.getPath().split("/");

        long questionId = Long.parseLong(segments[2]);

        long fromDate = link.getLastUpdated().toEpochSecond() + 1;

        var answersTask = CompletableFuture.supplyAsync(() ->
            stackOverflowClient.getAnswers(questionId, fromDate, SITE, FILTER));
        var commentsTask = CompletableFuture.supplyAsync(() ->
            stackOverflowClient.getComments(questionId, fromDate, SITE, FILTER));

        return Stream.of(
                processItems(answersTask.join().items(), "Новый ответ"),
                processItems(commentsTask.join().items(), "Новый комментарий")
            )
            .flatMap(List::stream)
            .sorted(Comparator.comparing(UpdateDescription::date))
            .toList();
    }

    private List<UpdateDescription> processItems(List<? extends StackOverflowBaseResponse> items, String eventType) {
        return items.stream()
            .map(item -> {
                String text = formatMessage(item, eventType);
                return new UpdateDescription(text, item.createdAt());
            })
            .toList();
    }

    private String formatMessage(StackOverflowBaseResponse item, String eventType) {
        if (item.body() == null) return "";
        String plain = item.body().replaceAll("<[^>]*>", "");
        return String.format(
            "Обновление на StackOverflow\nТема: %s\nСобытие: %s\nАвтор: %s\nДата: %s\n\n%s",
            item.title() != null ? item.title() : "Вопрос",
            eventType,
            item.owner().displayName(),
            item.createdAt(),
            plain.length() > 200 ? plain.substring(0, 200) + "..." : plain
        );
    }

    @Override
    public LinkType getType() {
        return LinkType.STACKOVERFLOW;
    }
}
