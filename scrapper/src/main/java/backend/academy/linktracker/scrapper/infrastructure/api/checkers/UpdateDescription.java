package backend.academy.linktracker.scrapper.infrastructure.api.checkers;

import java.time.OffsetDateTime;

public record UpdateDescription(String message, String author, OffsetDateTime date) {}
