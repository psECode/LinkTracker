package backend.academy.linktracker.scrapper.infrastructure.api.dtos;

import java.util.List;
import java.util.UUID;

public record LinkUpdate(Long id, String description, String author, List<Long> tgChatIds, List<UUID> userIds) {}
