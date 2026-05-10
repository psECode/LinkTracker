package backend.academy.linktracker.scrapper.updaters;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.domain.api.github.GithubClient;
import backend.academy.linktracker.scrapper.domain.api.github.GithubResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.GithubLinkChecker;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GithubCheckerTest {

    @Mock
    private GithubClient githubClient;

    @InjectMocks
    private GithubLinkChecker checker;

    @Test
    void checkerTest() {
        String url = "https://github.com/google/guava";
        OffsetDateTime expectedDate = OffsetDateTime.parse("2024-03-01T10:00:00Z");

        when(githubClient.getRepository("google", "guava")).thenReturn(new GithubResponse(expectedDate));

        OffsetDateTime result = checker.getLastUpdatedDate(url);

        assertThat(result).isEqualTo(expectedDate);
        verify(githubClient).getRepository("google", "guava");
    }

    @Test
    void checkerGitTest() {
        String url = "https://github.com/user/my-repo.git";
        OffsetDateTime expectedDate = OffsetDateTime.now();

        when(githubClient.getRepository("user", "my-repo.git")).thenReturn(new GithubResponse(expectedDate));

        OffsetDateTime result = checker.getLastUpdatedDate(url);

        assertThat(result).isNotNull();
    }
}
