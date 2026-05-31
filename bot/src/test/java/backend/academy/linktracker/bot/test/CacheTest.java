package backend.academy.linktracker.bot.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.api.ScrapperCachedService;
import backend.academy.linktracker.bot.domain.api.ScrapperClient;
import backend.academy.linktracker.bot.domain.api.dtos.AddLinkRequest;
import backend.academy.linktracker.bot.domain.api.dtos.ListLinksResponse;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class CacheTest {

    @Autowired
    private ScrapperCachedService cachedService;

    @MockitoBean
    private ScrapperClient scrapperClient;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        cacheManager.getCache("links").clear();
    }

    @Test
    void shouldReturnCachedValueEvenWhenClientChanges() {
        // g
        Long chatId = 123L;
        ListLinksResponse firstResponse = new ListLinksResponse(List.of(), 1);
        ListLinksResponse secondResponse = new ListLinksResponse(List.of(), 999);

        when(scrapperClient.getAllLinks(chatId)).thenReturn(firstResponse);

        // w
        cachedService.getAllLinks(chatId);

        // t
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            when(scrapperClient.getAllLinks(chatId)).thenReturn(secondResponse);
            ListLinksResponse result = cachedService.getAllLinks(chatId);
            assertEquals(1, result.size());
        });
    }

    @Test
    void shouldFetchFromNetworkAfterInvalidation() {
        // g
        Long chatId = 456L;
        ListLinksResponse oldResponse = new ListLinksResponse(List.of(), 1);
        ListLinksResponse newResponse = new ListLinksResponse(List.of(), 2);

        when(scrapperClient.getAllLinks(chatId)).thenReturn(oldResponse);

        // w
        cachedService.addLink(chatId, new AddLinkRequest(URI.create("http://test.com"), List.of()));

        // t
        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    when(scrapperClient.getAllLinks(chatId)).thenReturn(newResponse);
                    ListLinksResponse resultAfterUpdate = cachedService.getAllLinks(chatId);
                    assertEquals(2, resultAfterUpdate.size());
                });
    }
}
