package backend.academy.linktracker.scrapper.infrastructure.api.auth.dtos;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}
