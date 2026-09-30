package backend.academy.linktracker.scrapper.infrastructure.api.auth.dtos;

import java.util.UUID;

public record MeResponse(UUID id, String email, boolean telegramLinked) {}
