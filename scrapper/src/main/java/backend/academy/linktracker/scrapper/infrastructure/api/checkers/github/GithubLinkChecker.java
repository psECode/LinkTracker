package backend.academy.linktracker.scrapper.infrastructure.api.checkers.github;

import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.LinkChecker;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.UpdateDescription;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.github.entities.GithubBaseResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.github.entities.GithubClient;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GithubLinkChecker implements LinkChecker {
    private final GithubClient githubClient;
    private final GithubProperties properties;

    @Override
    public List<UpdateDescription> checkUpdates(Link link) {
        URI uri = URI.create(link.getUrl());
        String[] parts = uri.getPath().substring(1).split("/");
        String owner = parts[0];
        String repo = parts[1];

        String since = link.getLastUpdated().atZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        var issuesTask = CompletableFuture.supplyAsync(() ->
                githubClient.getLatestIssues(owner, repo, "all", "created", "desc", properties.getIssuesPerOnce()));

        var issueCommentsTask = CompletableFuture.supplyAsync(() ->
                githubClient.getIssueComments(owner, repo, "all", "created", since, properties.getIssuesPerOnce()));

        var prCommentsTask = CompletableFuture.supplyAsync(() -> githubClient.getPullRequestComments(
                owner, repo, "all", "created", since, properties.getIssuesPerOnce()));

        return Stream.of(
                        process(issuesTask.join(), "Issue/PR", link.getLastUpdated()),
                        process(issueCommentsTask.join(), "Комментарий к Issue", link.getLastUpdated()),
                        process(prCommentsTask.join(), "Комментарий к PR", link.getLastUpdated()))
                .flatMap(List::stream)
                .sorted(Comparator.comparing(UpdateDescription::date))
                .toList();
    }

    private List<UpdateDescription> process(
            List<? extends GithubBaseResponse> items, String eventType, OffsetDateTime lastUpdated) {
        return items.stream()
                .filter(i -> i.createdAt().isAfter(lastUpdated))
                .map(i -> {
                    String text = formatMessage(i.title(), i.user().login(), i.createdAt(), eventType, i.body());
                    return new UpdateDescription(text, i.createdAt());
                })
                .toList();
    }

    private String formatMessage(String title, String author, OffsetDateTime date, String type, String body) {
        String preview = body != null ? body : "";
        if (preview.length() > 200) {
            preview = preview.substring(0, 200) + "...";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Обновление на GitHub")
                .append(System.lineSeparator())
                .append("Репозиторий: ")
                .append(title)
                .append(System.lineSeparator())
                .append("Событие: ")
                .append(type)
                .append(System.lineSeparator())
                .append("Автор: ")
                .append(author)
                .append(System.lineSeparator())
                .append("Дата: ")
                .append(date)
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append(preview);

        return sb.toString();
    }

    @Override
    public LinkType getType() {
        return LinkType.GITHUB;
    }
}
