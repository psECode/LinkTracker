package backend.academy.linktracker.scrapper.domain.api;

import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkUpdate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface BotClient {
    @PostExchange("/updates")
    void sendUpdate(@RequestBody LinkUpdate update);
}
