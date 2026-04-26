package backend.academy.linktracker.scrapper.infrastructure.api.updateSenders;

import backend.academy.linktracker.scrapper.infrastructure.api.BotClient;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkUpdate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpLinkUpdateSender implements LinkUpdateSender {
    private final BotClient botClient;

    @Override
    public void send(LinkUpdate update) {
        botClient.sendUpdate(update);
    }
}
