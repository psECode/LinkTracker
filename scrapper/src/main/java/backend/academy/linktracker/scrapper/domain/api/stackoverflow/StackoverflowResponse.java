package backend.academy.linktracker.scrapper.domain.api.stackoverflow;

import java.util.List;

public record StackoverflowResponse(List<StackoverflowItem> items) {}
