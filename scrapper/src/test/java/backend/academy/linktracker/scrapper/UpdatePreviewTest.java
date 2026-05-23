package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.github.GithubLinkChecker;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.github.entities.GithubClient;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.github.entities.GithubCommentResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.github.entities.GithubIssueResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.github.entities.GithubUser;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdatePreviewTest {
    @Mock
    private GithubClient githubClient;

    @Mock
    private GithubProperties githubProperties;

    @InjectMocks
    private GithubLinkChecker githubLinkChecker;

    private final String owner = "user";
    private final String repo = "repo";
    private final Link link = Link.builder()
            .url("https://github.com/" + owner + "/" + repo)
            .lastUpdated(OffsetDateTime.parse("2024-01-01T00:00:00Z"))
            .build();

    @BeforeEach
    void setUp() {
        lenient().when(githubProperties.getIssuesPerOnce()).thenReturn(10);
    }

    @Test
    void truncatePreviewTest() {
        // g
        String body = "A".repeat(300);
        String title = "test";
        String user = "me";

        // w
        var issue = new GithubIssueResponse(title, body, link.getLastUpdated().plusHours(1), new GithubUser(user));
        when(githubClient.getLatestIssues(eq(owner), eq(repo), any(), any(), any(), any()))
                .thenReturn(List.of(issue));
        when(githubClient.getIssueComments(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(githubClient.getPullRequestComments(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        var updates = githubLinkChecker.checkUpdates(link);
        String message = updates.getFirst().message();

        // t
        assertThat(message).contains(title);
        assertThat(message).contains(user);
        String previewPart = message.substring(message.lastIndexOf("\n\n") + 2);
        assertThat(previewPart).hasSize(200 + 3);
        assertThat(previewPart).endsWith("...");
    }

    @Test
    void fullPreviewTest() {
        // g
        String body = "A".repeat(123);
        String title = "test";
        String user = "me";

        // w
        var issue = new GithubIssueResponse(title, body, link.getLastUpdated().plusHours(1), new GithubUser(user));
        when(githubClient.getLatestIssues(eq(owner), eq(repo), any(), any(), any(), any()))
                .thenReturn(List.of(issue));
        when(githubClient.getIssueComments(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(githubClient.getPullRequestComments(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        var updates = githubLinkChecker.checkUpdates(link);
        String message = updates.getFirst().message();

        // t
        assertThat(message).contains(title);
        assertThat(message).contains(user);
        String previewPart = message.substring(message.lastIndexOf("\n\n") + 2);
        assertThat(previewPart).hasSize(123);
    }

    @Test
    void shouldCollectAndSortDifferentUpdates() {
        // g
        var issue =
                new GithubIssueResponse("issue", "text", link.getLastUpdated().plusHours(2), new GithubUser("user1"));
        var comment = new GithubCommentResponse("comment", link.getLastUpdated().plusHours(1), new GithubUser("user2"));

        when(githubClient.getLatestIssues(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(issue));
        when(githubClient.getIssueComments(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(comment));
        when(githubClient.getPullRequestComments(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        // w
        var updates = githubLinkChecker.checkUpdates(link);

        // t
        assertThat(updates).hasSize(2);

        assertThat(updates.get(0).date()).isEqualTo(comment.createdAt());
        assertThat(updates.get(1).date()).isEqualTo(issue.createdAt());

        assertThat(updates.get(0).message()).contains("Комментарий к Issue");
        assertThat(updates.get(1).message()).contains("Issue/PR");
    }

    @Test
    void shouldReturnEmptyListWhenNoNewEvents() {
        // g
        when(githubClient.getLatestIssues(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(githubClient.getIssueComments(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(githubClient.getPullRequestComments(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        // w
        var updates = githubLinkChecker.checkUpdates(link);

        // t
        assertThat(updates).isEmpty();
    }
}
