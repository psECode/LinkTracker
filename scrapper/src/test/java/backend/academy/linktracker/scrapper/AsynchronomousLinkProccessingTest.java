package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.application.links.usecases.ReadTrackedLinkService;
import backend.academy.linktracker.scrapper.application.links.usecases.UpdateTrackedLinkTimeUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadUsersUuidsByLinkIdUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserService;
import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.LinkUpdateScheduler;
import backend.academy.linktracker.scrapper.infrastructure.api.LinkUpdater;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.LinkUpdateReport;
import backend.academy.linktracker.scrapper.infrastructure.api.checkers.UpdateDescription;
import backend.academy.linktracker.scrapper.infrastructure.api.updateSenders.LinkUpdateSender;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AsynchronomousLinkProccessingTest {

    @Mock
    private ReadTrackedLinkService readTrackedLinkService;

    @Mock
    private UpdateTrackedLinkTimeUseCase updateTrackedLinkTimeUseCase;

    @Mock
    private ReadUsersUuidsByLinkIdUseCase readUsersUuidsByLinkIdUseCase;

    @Mock
    private ReadUserService readUserService;

    @Mock
    private SchedulerProperties properties;

    @Mock
    private LinkUpdater linkUpdater;

    @Mock
    private LinkUpdateSender linkUpdateSender;

    @InjectMocks
    private LinkUpdateScheduler scheduler;

    @Test
    void schedulerUsingMultipleThreads() {
        // g
        int batchSize = 2;
        Link link1 = createLink("https://github.com/1");
        Link link2 = createLink("https://github.com/2");

        when(properties.getBatchSize()).thenReturn(batchSize);
        when(readTrackedLinkService.readExpiredLinks(batchSize)).thenReturn(List.of(link1, link2));

        when(readUsersUuidsByLinkIdUseCase.execute(any())).thenReturn(List.of(UUID.randomUUID()));
        when(readUserService.readByUUID(any()))
                .thenReturn(Optional.of(User.builder().chatId(1L).build()));

        Set<String> threadNames = ConcurrentHashMap.newKeySet();

        when(linkUpdater.process(any(Link.class))).thenAnswer(invocation -> {
            threadNames.add(Thread.currentThread().toString());
            return new LinkUpdateReport(invocation.getArgument(0), List.of(), null);
        });

        // w
        scheduler.update();

        // t
        verify(linkUpdater, times(2)).process(any(Link.class));

        assertThat(threadNames).hasSize(2);
    }

    @Test
    void errorsDontStopUpdater() throws Exception {
        // g
        Link badLink = createLink("https://github.com/bad");
        Link goodLink = createLink("https://github.com/good");

        when(properties.getBatchSize()).thenReturn(10);
        when(readTrackedLinkService.readExpiredLinks(10)).thenReturn(List.of(badLink, goodLink));

        List<UUID> uuids = List.of(UUID.randomUUID());
        when(readUsersUuidsByLinkIdUseCase.execute(any())).thenReturn(uuids);
        when(readUserService.readByUUID(any()))
                .thenReturn(Optional.of(User.builder().chatId(1L).build()));

        when(linkUpdater.process(badLink)).thenThrow(new RuntimeException("API Fatal Error"));

        UpdateDescription update = new UpdateDescription("New Issue Created", "author", OffsetDateTime.now());
        when(linkUpdater.process(goodLink)).thenReturn(new LinkUpdateReport(goodLink, List.of(update), null));

        // w
        scheduler.update();

        // t
        verify(linkUpdateSender)
                .send(argThat(u -> u.id().equals(goodLink.getId().getMostSignificantBits())
                        && u.description().equals("New Issue Created")
                        && u.author().equals("author")
                        && u.tgChatIds().contains(1L)));

        verify(updateTrackedLinkTimeUseCase).execute(argThat(dto -> dto.linkId().equals(goodLink.getId())));
        verify(updateTrackedLinkTimeUseCase, never())
                .execute(argThat(dto -> dto.linkId().equals(badLink.getId())));

        verify(linkUpdateSender, never())
                .send(argThat(u -> u.id().equals(badLink.getId().getMostSignificantBits())));
    }

    @Test
    void errorsAreHandledAndNotifiesUser() throws Exception {
        // g
        Link link = createLink("https://github.com/error-report");
        when(properties.getBatchSize()).thenReturn(10);
        when(readTrackedLinkService.readExpiredLinks(10)).thenReturn(List.of(link));

        when(readUsersUuidsByLinkIdUseCase.execute(any())).thenReturn(List.of(UUID.randomUUID()));
        when(readUserService.readByUUID(any()))
                .thenReturn(Optional.of(User.builder().chatId(123L).build()));

        LinkUpdateReport errorReport = new LinkUpdateReport(link, List.of(), "Not Found");
        when(linkUpdater.process(link)).thenReturn(errorReport);

        // w
        scheduler.update();

        // t
        verify(linkUpdateSender).send(argThat(u -> u.description().contains("Ошибка при проверке ссылки: Not Found")));
    }

    private Link createLink(String url) {
        return Link.builder()
                .id(UUID.randomUUID())
                .url(url)
                .type(LinkType.GITHUB)
                .checkInterval(Duration.ofMinutes(5))
                .build();
    }
}
