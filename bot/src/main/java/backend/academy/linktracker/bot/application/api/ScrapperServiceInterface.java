package backend.academy.linktracker.bot.application.api;

import backend.academy.linktracker.bot.domain.api.dtos.AddLinkRequest;
import backend.academy.linktracker.bot.domain.api.dtos.LinkResponse;
import backend.academy.linktracker.bot.domain.api.dtos.ListLinksResponse;
import backend.academy.linktracker.bot.domain.api.dtos.RemoveLinkRequest;

public interface ScrapperServiceInterface {
    ListLinksResponse getAllLinks(Long chatId);

    LinkResponse addLink(Long chatId, AddLinkRequest request);

    LinkResponse removeLink(Long chatId, RemoveLinkRequest request);

    void registerChat(Long chatId);
}
