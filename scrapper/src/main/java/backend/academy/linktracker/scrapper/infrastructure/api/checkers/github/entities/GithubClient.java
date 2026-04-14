package backend.academy.linktracker.scrapper.infrastructure.api.checkers.github.entities;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import java.util.List;

public interface GithubClient {
    @GetExchange("/repos/{owner}/{repo}/issues")
    List<GithubIssueResponse> getLatestIssues(
        @PathVariable String owner,
        @PathVariable String repo,
        @RequestParam("state") String state,
        @RequestParam("sort") String sort,
        @RequestParam("direction") String direction,
        @RequestParam("per_page") Integer perPage
    );

    @GetExchange("/repos/{owner}/{repo}/issues/comments")
    List<GithubCommentResponse> getIssueComments(
        @PathVariable String owner,
        @PathVariable String repo,
        @RequestParam("sort") String sort,
        @RequestParam("direction") String direction,
        @RequestParam("since") String since,
        @RequestParam("per_page") Integer perPage
    );

    @GetExchange("/repos/{owner}/{repo}/pulls/comments")
    List<GithubCommentResponse> getPullRequestComments(
        @PathVariable String owner,
        @PathVariable String repo,
        @RequestParam("sort") String sort,
        @RequestParam("direction") String direction,
        @RequestParam("since") String since,
        @RequestParam("per_page") Integer perPage
    );
}
