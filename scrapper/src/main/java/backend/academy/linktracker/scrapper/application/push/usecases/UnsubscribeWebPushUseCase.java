package backend.academy.linktracker.scrapper.application.push.usecases;

import backend.academy.linktracker.scrapper.domain.push.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnsubscribeWebPushUseCase {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    public void execute(String endpoint) {
        pushSubscriptionRepository.deleteByEndpoint(endpoint);
    }
}
