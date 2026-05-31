package backend.academy.linktracker.scrapper.infrastructure.api.dtos;

import java.util.List;

public record LinkUpdate(Long id, String description, String author, List<Long> tgChatIds) {}
