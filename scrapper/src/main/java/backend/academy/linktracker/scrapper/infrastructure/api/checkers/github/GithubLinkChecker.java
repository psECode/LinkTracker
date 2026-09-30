package backend.academy.linktracker.scrapper.infrastructure.api.checkers.github;

import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.LinkChecker;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.UpdateDescription;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.github.entities.GithubBaseResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.github.entities.GithubClient;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GithubLinkChecker implements LinkChecker {
    private final GithubClient githubClient;
    private final GithubProperties properties;
    private final MeterRegistry meterRegistry;

    @Override
    public List<UpdateDescription> checkUpdates(Link link) {
        var timer = meterRegistry.timer(
                "request_duration_ms_total", "scope", "external_source", "scope_type", "github.com");

        return timer.record(() -> {
            URI uri = URI.create(link.getUrl());
            String[] parts = uri.getPath().substring(1).split("/");
            String owner = parts[0];
            String repo = parts[1];

            String since =
                    link.getLastUpdated().atZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

            var issuesTask = CompletableFuture.supplyAsync(() ->
                    githubClient.getLatestIssues(owner, repo, "all", "created", "desc", properties.getIssuesPerOnce()));

            var issueCommentsTask = CompletableFuture.supplyAsync(() ->
                    githubClient.getIssueComments(owner, repo, "all", "created", since, properties.getIssuesPerOnce()));

            var prCommentsTask = CompletableFuture.supplyAsync(() -> githubClient.getPullRequestComments(
                    owner, repo, "all", "created", since, properties.getIssuesPerOnce()));

            var issues = issuesTask.join();
            var issueComments = issueCommentsTask.join();
            var prComments = prCommentsTask.join();

            List<UpdateDescription> updates = Stream.of(
                            process(issues, "Issue/PR", link.getLastUpdated()),
                            process(issueComments, "Комментарий к Issue", link.getLastUpdated()),
                            process(prComments, "Комментарий к PR", link.getLastUpdated()))
                    .flatMap(List::stream)
                    .sorted(Comparator.comparing(UpdateDescription::date))
                    .toList();

            log.info(
                    "GitHub {}/{}: since={}, получено: issues={}, комм. к issues={}, комм. к PR={}, обновлений после фильтра={}",
                    owner,
                    repo,
                    since,
                    issues.size(),
                    issueComments.size(),
                    prComments.size(),
                    updates.size());

            return updates;
        });
    }

    private List<UpdateDescription> process(
            List<? extends GithubBaseResponse> items, String eventType, OffsetDateTime lastUpdated) {
        return items.stream()
                .filter(i -> i.createdAt().isAfter(lastUpdated))
                .map(i -> {
                    String text = formatMessage(i.title(), i.user().login(), i.createdAt(), eventType, i.body());
                    return new UpdateDescription(text, i.user().login(), i.createdAt());
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
