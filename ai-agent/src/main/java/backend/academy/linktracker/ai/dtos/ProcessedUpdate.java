package backend.academy.linktracker.ai.dtos;

import java.util.List;

public record ProcessedUpdate(Long id, String description, List<Long> tgChatIds, String priority) {}
