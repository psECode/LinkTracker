package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.application.links.usecases.ReadTrackedLinkService;
import backend.academy.linktracker.scrapper.application.links.usecases.UpdateTrackedLinkTimeUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadUsersUuidsByLinkIdUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserService;
import backend.academy.linktracker.scrapper.domain.links.dtos.UpdateTimeDTO;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.LinkUpdateReport;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.UpdateDescription;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkUpdate;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LinkUpdateScheduler {

    private final ReadTrackedLinkService readTrackedLinkService;
    private final UpdateTrackedLinkTimeUseCase updateTrackedLinkTimeUseCase;
    private final ReadUsersUuidsByLinkIdUseCase readUsersUuidsByLinkIdUseCase;
    private final ReadUserService readUserService;
    private final SchedulerProperties properties;

    private final LinkUpdater linkUpdater;
    private final BotClient botClient;

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

            List<Long> chatIds = getChatIdsForLink(link.getId());
            if (chatIds.isEmpty()) return;

            String description = null;
            if (report.errorMessage() != null) {
                description = "Ошибка при проверке ссылки: " + report.errorMessage();
            } else if (!report.updates().isEmpty()) {
                description = report.updates().stream()
                    .map(UpdateDescription::message)
                    .collect(Collectors.joining("\n\n"));
            }

            if (description != null) {
                botClient.sendUpdate(new LinkUpdate(
                    link.getId().getMostSignificantBits(),
                    URI.create(link.getUrl()),
                    description,
                    chatIds
                ));
            }

            UpdateTimeDTO updateTimeDTO = new UpdateTimeDTO(link.getId(), !report.updates().isEmpty());
            updateTrackedLinkTimeUseCase.execute(updateTimeDTO);

        } catch (Exception e) {
            log.error("error {}: {}", link.getUrl(), e.getMessage());
        }
    }

    private List<Long> getChatIdsForLink(UUID linkId) {
        return readUsersUuidsByLinkIdUseCase.execute(linkId).stream()
            .map(readUserService::readByUUID)
            .flatMap(Optional::stream)
            .map(User::getChatId)
            .toList();
    }
}
