package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.application.links.usecases.ReadTrackedLinkService;
import backend.academy.linktracker.scrapper.application.links.usecases.UpdateTrackedLinkTimeUseCase;
import backend.academy.linktracker.scrapper.application.notifications.usecases.SaveNotificationsUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadUsersUuidsByLinkIdUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserService;
import backend.academy.linktracker.scrapper.domain.links.dtos.UpdateTimeDTO;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.LinkUpdateReport;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.UpdateDescription;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkUpdate;
import backend.academy.linktracker.scrapper.infrastructure.api.updateSenders.LinkUpdateSender;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkUpdateScheduler {

    private final ReadTrackedLinkService readTrackedLinkService;
    private final UpdateTrackedLinkTimeUseCase updateTrackedLinkTimeUseCase;
    private final ReadUsersUuidsByLinkIdUseCase readUsersUuidsByLinkIdUseCase;
    private final ReadUserService readUserService;
    private final SaveNotificationsUseCase saveNotificationsUseCase;
    private final SchedulerProperties properties;

    private final LinkUpdater linkUpdater;
    private final LinkUpdateSender sender;

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Scheduled(fixedDelayString = "${app.scheduler.interval}")
    public void update() {
        List<Link> links = readTrackedLinkService.readExpiredLinks(properties.getBatchSize());
        if (links.isEmpty()) return;

        log.info("Обработка {} ссылок", links.size());

        var futures = links.stream()
                .map(link -> CompletableFuture.runAsync(() -> handleLink(link), executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
    }

    private void handleLink(Link link) {
        try {
            LinkUpdateReport report = linkUpdater.process(link);
            List<UUID> userIds = readUsersUuidsByLinkIdUseCase.execute(link.getId());
            if (userIds.isEmpty()) {
                log.info("Ссылка {}: обновлений {}, подписчиков нет — пропуск", link.getUrl(), report.updates().size());
                return;
            }

            List<Long> tgChatIds = resolveTelegramIds(userIds);

            if (report.errorMessage() != null) {
                log.warn("Ссылка {}: ошибка проверки '{}', рассылаю уведомление {} подписчикам (telegram: {})",
                        link.getUrl(), report.errorMessage(), userIds.size(), tgChatIds.size());
                notifySubscribers(
                        link, userIds, tgChatIds, "Ошибка при проверке ссылки: " + report.errorMessage(), null);
            } else if (report.updates().isEmpty()) {
                log.info("Ссылка {}: обновлений нет, подписчиков {}, telegram {}",
                        link.getUrl(), userIds.size(), tgChatIds.size());
            } else {
                log.info("Ссылка {}: найдено {} обновлений, подписчиков {}, telegram {}",
                        link.getUrl(), report.updates().size(), userIds.size(), tgChatIds.size());
                for (UpdateDescription update : report.updates()) {
                    log.info("Ссылка {}: событие от {} ({}): {}",
                            link.getUrl(), update.author(), update.date(), shorten(update.message()));
                    notifySubscribers(link, userIds, tgChatIds, update.message(), update.author());
                }
            }

            UpdateTimeDTO updateTimeDTO =
                    new UpdateTimeDTO(link.getId(), !report.updates().isEmpty());
            updateTrackedLinkTimeUseCase.execute(updateTimeDTO);

        } catch (Exception e) {
            log.error("error {}: {}", link.getUrl(), e.getMessage());
        }
    }

    private String shorten(String message) {
        String oneLine = message.replace(System.lineSeparator(), " | ");
        return oneLine.length() <= 120 ? oneLine : oneLine.substring(0, 117) + "...";
    }

    private void notifySubscribers(Link link, List<UUID> userIds, List<Long> tgChatIds, String message, String author)
            throws Exception {
        sender.send(new LinkUpdate(link.getId().getMostSignificantBits(), message, author, tgChatIds, userIds));
        saveNotificationsUseCase.execute(link.getId(), userIds, message);
    }

    private List<Long> resolveTelegramIds(List<UUID> userIds) {
        return userIds.stream()
                .map(readUserService::readByUUID)
                .flatMap(Optional::stream)
                .map(User::getTelegramId)
                .filter(Objects::nonNull)
                .toList();
    }
}
