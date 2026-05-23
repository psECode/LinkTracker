package backend.academy.linktracker.scrapper.infrastructure.api.usecases;

import backend.academy.linktracker.scrapper.application.links.usecases.ReadTrackedLinkService;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.DeleteSubscriptionUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadSubscriptionService;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserService;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.ReadSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.SubscriptionResult;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.LinkNotFoundException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.SubscriptionNotFoundException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnsubscribeUserUseCase {

    private final ReadUserService readUserService;
    private final ReadTrackedLinkService readTrackedLinkService;
    private final ReadSubscriptionService readSubscriptionService;
    private final DeleteSubscriptionUseCase deleteSubscriptionUseCase;

    @Transactional
    public SubscriptionResult execute(Long chatId, String url) {
        User user = readUserService.readByChatId(chatId).orElseThrow(() -> new UserNotFoundException(chatId));

        Link link = readTrackedLinkService.readByUrl(url).orElseThrow(() -> new LinkNotFoundException(url));

        var readDto = new ReadSubscriptionDTO(user.getId(), link.getId());
        Subscription sub =
                readSubscriptionService.read(readDto).orElseThrow(() -> new SubscriptionNotFoundException(chatId, url));

        // Вызов атомарного удаления
        deleteSubscriptionUseCase.execute(sub.getId());

        return new SubscriptionResult(sub, link, user);
    }
}
