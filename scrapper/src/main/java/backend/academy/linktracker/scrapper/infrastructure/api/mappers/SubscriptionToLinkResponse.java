package backend.academy.linktracker.scrapper.infrastructure.api.mappers;

import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkResponse;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionToLinkResponse {

    public LinkResponse map(Subscription subscription, Link link, User user) {
        if (link == null) {
            throw new IllegalArgumentException("чево вы сюда прислали вообще");
        }

        return new LinkResponse(user.getChatId(), URI.create(link.getUrl()), subscription.getTags());
    }
}
