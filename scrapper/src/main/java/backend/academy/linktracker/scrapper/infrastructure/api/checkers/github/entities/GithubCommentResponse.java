package backend.academy.linktracker.scrapper.infrastructure.api.checkers.github.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record GithubCommentResponse(
        String body, @JsonProperty("created_at") OffsetDateTime createdAt, GithubUser user)
        implements GithubBaseResponse {
    @Override
    public String title() {
        return "Комментарий";
    }
}
