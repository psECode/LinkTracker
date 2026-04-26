package backend.academy.linktracker.scrapper.infrastructure.api.updateSenders;

import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkUpdate;

public interface LinkUpdateSender {
    void send(LinkUpdate update) throws Exception;
}
