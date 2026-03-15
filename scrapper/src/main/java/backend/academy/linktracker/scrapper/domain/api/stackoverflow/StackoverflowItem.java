package backend.academy.linktracker.scrapper.domain.api.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record StackoverflowItem(
        @JsonProperty("last_activity_date") long lastActivityDate,
        @JsonProperty("question_id") long questionId) {
    public OffsetDateTime lastActivityAsOffsetDateTime() {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(lastActivityDate), ZoneOffset.UTC);
    }
}
