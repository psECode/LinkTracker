package backend.academy.linktracker.scrapper.infrastructure.api.checkers.github.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GithubIssueResponse(
    String title,
    String body,
    @JsonProperty("created_at") OffsetDateTime createdAt,
    GithubUser user
) implements GithubBaseResponse {}
