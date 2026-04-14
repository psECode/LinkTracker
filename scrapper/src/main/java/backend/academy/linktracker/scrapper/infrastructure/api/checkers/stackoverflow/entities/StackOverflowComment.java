package backend.academy.linktracker.scrapper.infrastructure.api.checkers.stackoverflow.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.OffsetDateTime;

public record StackOverflowComment(
    String body,
    @JsonProperty("creation_date")
    @JsonDeserialize(using = UnixTimestampDeserializer.class)
    OffsetDateTime createdAt,
    StackOverflowUser owner
) implements StackOverflowBaseResponse {
    @Override
    public String title() { return "Комментарий к вопросу"; }
}
