package backend.academy.linktracker.scrapper.infrastructure.api.checkers;

import backend.academy.linktracker.scrapper.domain.api.github.GithubClient;
import backend.academy.linktracker.scrapper.domain.links.LinkType;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GithubLinkChecker implements LinkChecker {
    private final GithubClient githubClient;

    @Override
    public OffsetDateTime getLastUpdatedDate(String url) {
        String[] parts = url.replaceFirst("https?://github\\.com/", "").split("/");
        return githubClient.getRepository(parts[0], parts[1]).updatedAt();
    }

    @Override
    public LinkType getType() {
        return LinkType.GITHUB;
    }
}
