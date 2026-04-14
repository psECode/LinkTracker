package backend.academy.linktracker.scrapper.infrastructure.api.checkers.stackoverflow.entities;

import java.util.List;

public record StackOverflowResponse<T>(
    List<T> items
) {}
