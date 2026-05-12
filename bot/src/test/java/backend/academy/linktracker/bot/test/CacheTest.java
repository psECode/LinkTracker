package backend.academy.linktracker.bot.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.api.ScrapperCachedService;
import backend.academy.linktracker.bot.domain.api.ScrapperClient;
import backend.academy.linktracker.bot.domain.api.dtos.AddLinkRequest;
import backend.academy.linktracker.bot.domain.api.dtos.ListLinksResponse;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
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

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        cacheManager.getCache("links").clear();
    }

    @Test
    void shouldReturnCachedValueEvenWhenClientChanges() {
        // Given
        Long chatId = 123L;
        ListLinksResponse firstResponse = new ListLinksResponse(List.of(), 1);
        ListLinksResponse secondResponse = new ListLinksResponse(List.of(), 999); // "Фейковый" новый ответ

        // Настраиваем мок на первый ответ
        when(scrapperClient.getAllLinks(chatId)).thenReturn(firstResponse);

        // When
        ListLinksResponse result1 = cachedService.getAllLinks(chatId);

        // СРАЗУ меняем поведение мока. Если бы кэша не было, следующий вызов вернул бы 999
        when(scrapperClient.getAllLinks(chatId)).thenReturn(secondResponse);

        ListLinksResponse result2 = cachedService.getAllLinks(chatId);

        // Then
        assertEquals(1, result1.size());
        assertEquals(1, result2.size()); // Ожидаем 1 (из кэша), а не 999
        assertEquals(result1, result2); // Они должны быть идентичны
    }

    @Test
    void shouldFetchFromNetworkAfterInvalidation() {
        // g
        Long chatId = 456L;
        ListLinksResponse oldResponse = new ListLinksResponse(List.of(), 1);
        ListLinksResponse newResponse = new ListLinksResponse(List.of(), 2);

        when(scrapperClient.getAllLinks(chatId)).thenReturn(oldResponse);
        cachedService.getAllLinks(chatId);

        // w
        cachedService.addLink(chatId, new AddLinkRequest(URI.create("http://test.com"), List.of()));

        when(scrapperClient.getAllLinks(chatId)).thenReturn(newResponse);

        ListLinksResponse resultAfterUpdate = cachedService.getAllLinks(chatId);

        // t
        assertEquals(2, resultAfterUpdate.size());
    }
}
