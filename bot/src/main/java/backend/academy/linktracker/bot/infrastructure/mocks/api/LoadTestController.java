package backend.academy.linktracker.bot.infrastructure.mocks.api;

import backend.academy.linktracker.bot.application.api.ScrapperServiceInterface;
import backend.academy.linktracker.bot.domain.api.dtos.ListLinksResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-cache")
@RequiredArgsConstructor
@Profile("test")
public class LoadTestController {

    private final ScrapperServiceInterface scrapperService;

    @GetMapping("/links")
    public ListLinksResponse testGetLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        return scrapperService.getAllLinks(chatId);
    }
}
