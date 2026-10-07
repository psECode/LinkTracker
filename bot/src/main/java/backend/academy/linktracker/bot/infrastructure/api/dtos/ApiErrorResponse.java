package backend.academy.linktracker.bot.infrastructure.api.dtos;

public record ApiErrorResponse(String description, String code, String exceptionName, String exceptionMessage) {}
