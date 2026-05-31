package backend.academy.linktracker.bot.application.api;

import backend.academy.linktracker.bot.domain.api.ScrapperClient;
import backend.academy.linktracker.bot.domain.api.dtos.AddLinkRequest;
import backend.academy.linktracker.bot.domain.api.dtos.LinkResponse;
import backend.academy.linktracker.bot.domain.api.dtos.ListLinksResponse;
import backend.academy.linktracker.bot.domain.api.dtos.RemoveLinkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "redis")
public class ScrapperCachedService implements ScrapperServiceInterface {

    private final ScrapperClient scrapperClient;

    public void registerChat(Long chatId) {
        scrapperClient.registerChat(chatId);
    }

    @Cacheable(cacheNames = "links", key = "#chatId")
    public ListLinksResponse getAllLinks(Long chatId) {
        return scrapperClient.getAllLinks(chatId);
    }

    @CacheEvict(cacheNames = "links", key = "#chatId")
    public LinkResponse addLink(Long chatId, AddLinkRequest request) {
        return scrapperClient.addLink(chatId, request);
    }

    @CacheEvict(cacheNames = "links", key = "#chatId")
    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request) {
        return scrapperClient.removeLink(chatId, request);
    }
}
