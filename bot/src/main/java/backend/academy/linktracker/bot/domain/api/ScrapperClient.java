package backend.academy.linktracker.bot.domain.api;

import backend.academy.linktracker.bot.domain.api.dtos.AddLinkRequest;
import backend.academy.linktracker.bot.domain.api.dtos.LinkResponse;
import backend.academy.linktracker.bot.domain.api.dtos.ListLinksResponse;
import backend.academy.linktracker.bot.domain.api.dtos.RemoveLinkRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

public interface ScrapperClient {

    @PostExchange("/internal/tg-chat/{id}")
    void registerChat(@PathVariable("id") Long id);

    @GetExchange("/internal/links")
    ListLinksResponse getAllLinks(@RequestHeader("Tg-Chat-Id") Long chatId);

    @PostExchange("/internal/links")
    LinkResponse addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request);

    @DeleteExchange("/internal/links")
    LinkResponse removeLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request);
}
