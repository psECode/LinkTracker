package backend.academy.linktracker.scrapper.infrastructure.api.checkers.github.entities;

import java.time.OffsetDateTime;

public interface GithubBaseResponse {
    String title();
    String body();
    OffsetDateTime createdAt();
    GithubUser user();
}
