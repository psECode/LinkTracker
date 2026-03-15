package backend.academy.linktracker.scrapper.domain.api.github;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface GithubClient {
    @GetExchange("/repos/{owner}/{repo}")
    GithubResponse getRepository(@PathVariable String owner, @PathVariable String repo);
}
