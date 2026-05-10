package backend.academy.linktracker.bot.application.api;

import backend.academy.linktracker.bot.domain.api.ScrapperClient;
import backend.academy.linktracker.bot.domain.api.dtos.AddLinkRequest;
import backend.academy.linktracker.bot.domain.api.dtos.LinkResponse;
import backend.academy.linktracker.bot.domain.api.dtos.ListLinksResponse;
import backend.academy.linktracker.bot.domain.api.dtos.RemoveLinkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "mock")
public class DirectScrapperService implements ScrapperServiceInterface {

    private final ScrapperClient scrapperClient;

    @Override
    public ListLinksResponse getAllLinks(Long chatId) {
        return scrapperClient.getAllLinks(chatId);
    }

    @Override
    public LinkResponse addLink(Long chatId, AddLinkRequest request) {
        return scrapperClient.addLink(chatId, request);
    }

    @Override
    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request) {
        return scrapperClient.removeLink(chatId, request);
    }

    @Override
    public void registerChat(Long chatId) {
        scrapperClient.registerChat(chatId);
    }
}
