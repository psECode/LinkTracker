package backend.academy.linktracker.bot.test;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.api.ScrapperCachedService;
import backend.academy.linktracker.bot.domain.api.ScrapperClient;
import backend.academy.linktracker.bot.domain.api.dtos.AddLinkRequest;
import backend.academy.linktracker.bot.domain.api.dtos.ListLinksResponse;
import backend.academy.linktracker.bot.domain.api.dtos.RemoveLinkRequest;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class CacheTest {

    @Autowired
    private ScrapperCachedService cachedService;

    @MockitoBean
    private ScrapperClient scrapperClient;

    @Test
    void shouldReturnLinksFromCacheOnSecondCall() {
        // g
        Long chatId = 1L;
        ListLinksResponse mockResponse = new ListLinksResponse(List.of(), 0);
        when(scrapperClient.getAllLinks(chatId)).thenReturn(mockResponse);

        // w
        cachedService.getAllLinks(chatId);
        cachedService.getAllLinks(chatId);

        // t
        verify(scrapperClient, times(1)).getAllLinks(chatId);
    }

    @Test
    void shouldInvalidateCacheWhenLinkAdded() {
        // g
        Long chatId = 2L;
        ListLinksResponse mockResponse = new ListLinksResponse(List.of(), 0);
        when(scrapperClient.getAllLinks(chatId)).thenReturn(mockResponse);
        cachedService.getAllLinks(chatId);
        verify(scrapperClient, times(1)).getAllLinks(chatId);

        // w
        cachedService.addLink(chatId, new AddLinkRequest(URI.create("https://github.com/new"), List.of()));
        cachedService.getAllLinks(chatId);

        // t
        verify(scrapperClient, times(2)).getAllLinks(chatId);
    }

    @Test
    void shouldInvalidateCacheWhenLinkRemoved() {
        // g
        Long chatId = 3L;
        ListLinksResponse mockResponse = new ListLinksResponse(List.of(), 0);
        when(scrapperClient.getAllLinks(chatId)).thenReturn(mockResponse);

        // w
        cachedService.getAllLinks(chatId);
        cachedService.removeLink(chatId, new RemoveLinkRequest(URI.create("https://github.com/old")));
        cachedService.getAllLinks(chatId);

        // t
        verify(scrapperClient, times(2)).getAllLinks(chatId);
    }

    @Test
    void shouldKeepCachesSeparatedByChatId() {
        // g
        Long user1 = 101L;
        Long user2 = 102L;
        when(scrapperClient.getAllLinks(anyLong())).thenReturn(new ListLinksResponse(List.of(), 0));

        // w
        cachedService.getAllLinks(user1);
        cachedService.getAllLinks(user2);

        // t
        verify(scrapperClient, times(1)).getAllLinks(user1);
        verify(scrapperClient, times(1)).getAllLinks(user2);
    }
}
