package backend.academy.linktracker.scrapper.infrastructure.api.dtos;

import java.util.List;

public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {}
