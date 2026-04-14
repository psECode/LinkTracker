package backend.academy.linktracker.scrapper.infrastructure.api.usecases;

import backend.academy.linktracker.scrapper.application.links.usecases.CreateTrackedLinkService;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.CreateSubscriptionUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadSubscriptionService;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserService;
import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.dtos.CreateTrackedLinkDTO;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.CreateSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.ReadSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.SubscriptionResult;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.InvalidLinkException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.UserNotFoundException;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscribeUserUseCase {

    private final ReadUserService readUserService;
    private final CreateTrackedLinkService createTrackedLinkService;
    private final ReadSubscriptionService readSubscriptionService;
    private final CreateSubscriptionUseCase createSubscriptionUseCase;
    private final SchedulerProperties properties;

    @Transactional
    public SubscriptionResult execute(Long chatId, URI url, List<String> tags) {
        User user = readUserService.readByChatId(chatId)
            .orElseThrow(() -> new UserNotFoundException(chatId));

        LinkType type = LinkType.of(url.toString())
            .orElseThrow(() -> new InvalidLinkException("Сервис не поддерживается"));

        OffsetDateTime now = OffsetDateTime.now();

        CreateTrackedLinkDTO dto = new CreateTrackedLinkDTO(
            url.toString(),
            now.plus(properties.getInterval()),
            type,
            now,
            properties.getLinkCheckInterval()
        );

        Link link = createTrackedLinkService.createLink(dto)
            .orElseThrow(() -> new RuntimeException("Ошибка при обработке ссылки"));

        ReadSubscriptionDTO readDto = new ReadSubscriptionDTO(user.getId(), link.getId());
        if (readSubscriptionService.read(readDto).isPresent()) {
            throw new LinkAlreadyTrackedException("Вы уже подписаны на эту ссылку");
        }

        Subscription sub = createSubscriptionUseCase.execute(new CreateSubscriptionDTO(user.getId(), link.getId(), tags))
            .orElseThrow(() -> new RuntimeException("Ошибка сохранения подписки"));

        return new SubscriptionResult(sub, link, user);
    }
}
