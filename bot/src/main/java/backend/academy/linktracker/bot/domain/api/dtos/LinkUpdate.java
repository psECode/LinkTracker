package backend.academy.linktracker.bot.domain.api.dtos;

import java.util.List;

public record LinkUpdate(Long id, String description, String priority, List<Long> tgChatIds) {}
