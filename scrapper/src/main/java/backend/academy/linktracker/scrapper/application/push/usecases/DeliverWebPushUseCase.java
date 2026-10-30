package backend.academy.linktracker.scrapper.application.push.usecases;

import backend.academy.linktracker.logging.LogArgs;
import backend.academy.linktracker.scrapper.domain.push.PushSubscription;
import backend.academy.linktracker.scrapper.domain.push.PushSubscriptionRepository;
import backend.academy.linktracker.scrapper.infrastructure.push.WebPushSender;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class DeliverWebPushUseCase {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final WebPushSender webPushSender;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public void execute(List<UUID> userIds, String message) {
        if (!webPushSender.enabled() || userIds == null || userIds.isEmpty()) {
            return;
        }

        int scheduled = 0;
        for (UUID userId : userIds) {
            List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUser(userId);
            for (PushSubscription subscription : subscriptions) {
                CompletableFuture.runAsync(() -> webPushSender.send(subscription, message), executor);
                scheduled++;
            }
        }
        if (scheduled > 0) {
            log.info("Web Push subscriptions scheduled", LogArgs.of("count", scheduled));
        }
    }
}
