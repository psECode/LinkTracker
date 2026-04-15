package backend.academy.linktracker.scrapper.infrastructure.api.checkers.stackoverflow.entities;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface StackOverflowClient {
    @GetExchange("/questions/{id}/answers")
    StackOverflowResponse<StackOverflowAnswer> getAnswers(
            @PathVariable("id") Long id,
            @RequestParam("fromdate") Long fromDate,
            @RequestParam("site") String site,
            @RequestParam("filter") String filter);

    /**
     * Получить список комментариев к вопросу
     */
    @GetExchange("/questions/{id}/comments")
    StackOverflowResponse<StackOverflowComment> getComments(
            @PathVariable("id") Long id,
            @RequestParam("fromdate") Long fromDate,
            @RequestParam("site") String site,
            @RequestParam("filter") String filter);
}
