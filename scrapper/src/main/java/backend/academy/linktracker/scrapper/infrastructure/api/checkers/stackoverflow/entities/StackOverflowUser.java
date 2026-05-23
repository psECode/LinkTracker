package backend.academy.linktracker.scrapper.infrastructure.api.checkers.stackoverflow.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowUser(
        @JsonProperty("display_name") String displayName) {}
