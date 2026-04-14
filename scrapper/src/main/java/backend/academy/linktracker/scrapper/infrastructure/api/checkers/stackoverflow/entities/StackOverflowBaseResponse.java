
package backend.academy.linktracker.scrapper.infrastructure.api.checkers.stackoverflow.entities;

import java.time.OffsetDateTime;

public interface StackOverflowBaseResponse {
    String title();
    String body();
    OffsetDateTime createdAt();
    StackOverflowUser owner();
}
