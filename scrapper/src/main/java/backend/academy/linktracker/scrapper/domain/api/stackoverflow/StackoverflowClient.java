package backend.academy.linktracker.scrapper.domain.api.stackoverflow;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface StackoverflowClient {
    @GetExchange("questions/{id}")
    StackoverflowResponse getQuestion(@PathVariable String id);
}
