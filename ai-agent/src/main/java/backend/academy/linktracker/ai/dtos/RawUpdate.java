package backend.academy.linktracker.ai.dtos;

import java.util.List;

public record RawUpdate(Long id, String description, String author, List<Long> tgChatIds) {}
