package backend.academy.linktracker.scrapper.links;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.mappers.SubscriptionToLinkResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MappersTest {
    private final SubscriptionToLinkResponse subscriptionToLinkResponse = new SubscriptionToLinkResponse();

    @Test
    void mapTest() {
        User user = User.builder().chatId(100L).build();
        Link link = Link.builder().url("https://github.com").build();
        Subscription sub = Subscription.builder().tags(List.of("tag1")).build();

        LinkResponse response = subscriptionToLinkResponse.map(sub, link, user);

        assertThat(response.url().toString()).isEqualTo("https://github.com");
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.tags()).containsExactly("tag1");
    }
}
