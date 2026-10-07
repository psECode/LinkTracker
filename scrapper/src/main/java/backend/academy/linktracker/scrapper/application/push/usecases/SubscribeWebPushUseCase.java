package backend.academy.linktracker.scrapper.application.push.usecases;

import backend.academy.linktracker.scrapper.domain.push.PushSubscription;
import backend.academy.linktracker.scrapper.domain.push.PushSubscriptionRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscribeWebPushUseCase {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    public void execute(UUID userId, String endpoint, String p256dh, String auth) {
        pushSubscriptionRepository.save(new PushSubscription(UUID.randomUUID(), userId, endpoint, p256dh, auth));
    }
}
