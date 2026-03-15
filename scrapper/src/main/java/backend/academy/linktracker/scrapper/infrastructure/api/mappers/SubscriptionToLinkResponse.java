package backend.academy.linktracker.scrapper.infrastructure.api.mappers;

import backend.academy.linktracker.scrapper.domain.api.dtos.LinkResponse;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionToLinkResponse {

    public LinkResponse map(Subscription subscription, Link link, User user) {
        if (link == null) {
            throw new IllegalArgumentException("TrackedLink cannot be null for mapping");
        }

        return new LinkResponse(user.getChatId(), URI.create(link.getUrl()), subscription.getTags());
    }
}
