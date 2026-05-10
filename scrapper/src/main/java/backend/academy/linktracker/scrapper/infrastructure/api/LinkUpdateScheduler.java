package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.application.links.usecases.ReadExpiredTrackedLinksUseCase;
import backend.academy.linktracker.scrapper.application.links.usecases.UpdateTrackedLinkTimeUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadUsersUuidsByLinkIdUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserByUUIDUseCase;
import backend.academy.linktracker.scrapper.domain.api.BotClient;
import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.LinkChecker;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkUpdate;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkUpdateScheduler {

    private final ReadExpiredTrackedLinksUseCase readyLinksUseCase;
    private final ReadUserByUUIDUseCase readUserByUUIDUseCase;
    private final BotClient botClient;
    private final ReadUsersUuidsByLinkIdUseCase readUsersUuidsByLinkIdUseCase;
    private final UpdateTrackedLinkTimeUseCase updateTrackedLinkTimeUseCase;
    private final Map<LinkType, LinkChecker> checkers;

    @Scheduled(fixedDelayString = "${app.scheduler.interval}")
    public void update() {
        log.info("Запуск проверки обновлений...");

        List<Link> links = readyLinksUseCase.execute();

        for (Link link : links) {
            try {
                log.info("Проверка ссылки: {}", link.getUrl());
                LinkChecker checker = checkers.get(link.getType());

                OffsetDateTime updateDate = checker.getLastUpdatedDate(link.getUrl());

                if (updateDate != null && updateDate.isAfter(link.getLastUpdated())) {
                    List<UUID> userIds = readUsersUuidsByLinkIdUseCase.execute(link.getId());

                    List<Long> chatIds = userIds.stream()
                            .map(readUserByUUIDUseCase::execute)
                            .flatMap(Optional::stream)
                            .map(User::getChatId)
                            .toList();

                    if (!chatIds.isEmpty()) {
                        LinkUpdate update = new LinkUpdate(
                                ThreadLocalRandom.current().nextLong(Long.MAX_VALUE),
                                URI.create(link.getUrl()),
                                "Появились новые изменения по ссылке!",
                                chatIds);

                        try {
                            botClient.sendUpdate(update);
                            log.info("Уведомление отправлено для {} пользователей", chatIds.size());
                        } catch (Exception e) {
                            log.error("Ошибка при отправке уведомления в Бот: {}", e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Ошибка при проверке ссылки {}: {}", link.getUrl(), e.getMessage());
            } finally {
                updateTrackedLinkTimeUseCase.execute(link.getId());
            }
        }
    }
}
