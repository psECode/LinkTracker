package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.application.links.usecases.ReadExpiredTrackedLinksUseCase;
import backend.academy.linktracker.scrapper.application.links.usecases.UpdateTrackedLinkTimeUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadUsersUuidsByLinkIdUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserByUUIDUseCase;
import backend.academy.linktracker.scrapper.domain.api.BotClient;
import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.LinkChecker;
import backend.academy.linktracker.scrapper.infrastructure.api.LinkUpdateScheduler;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SchedulerTest {

    @Mock
    private ReadExpiredTrackedLinksUseCase readyLinksUseCase;

    @Mock
    private ReadUserByUUIDUseCase readUserByUUIDUseCase;

    @Mock
    private BotClient botClient;

    @Mock
    private ReadUsersUuidsByLinkIdUseCase readUsersUuidsByLinkIdUseCase;

    @Mock
    private UpdateTrackedLinkTimeUseCase updateTrackedLinkTimeUseCase;

    @Mock
    private LinkChecker githubChecker;

    private LinkUpdateScheduler scheduler;

    @BeforeEach
    void setUp() {
        Map<LinkType, LinkChecker> checkers = Map.of(LinkType.GITHUB, githubChecker);

        scheduler = new LinkUpdateScheduler(
                readyLinksUseCase,
                readUserByUUIDUseCase,
                botClient,
                readUsersUuidsByLinkIdUseCase,
                updateTrackedLinkTimeUseCase,
                checkers);
    }

    @Test
    void HasUpdatesTest() {
        UUID linkId = UUID.randomUUID();
        UUID userUuid = UUID.randomUUID();
        Link link = createSampleLink(OffsetDateTime.parse("2023-01-01T10:00:00Z"), linkId);

        when(readyLinksUseCase.execute()).thenReturn(List.of(link));
        when(githubChecker.getLastUpdatedDate(anyString())).thenReturn(OffsetDateTime.parse("2024-01-01T10:00:00Z"));
        when(readUsersUuidsByLinkIdUseCase.execute(linkId)).thenReturn(List.of(userUuid));
        when(readUserByUUIDUseCase.execute(userUuid))
                .thenReturn(Optional.of(User.builder().chatId(12345L).build()));

        scheduler.update();

        verify(botClient, times(1)).sendUpdate(any());
        verify(updateTrackedLinkTimeUseCase).execute(linkId);
    }

    @Test
    void HaveNotUpdatesTest() {
        Link link = createSampleLink(OffsetDateTime.parse("2024-01-01T10:00:00Z"), UUID.randomUUID());

        when(readyLinksUseCase.execute()).thenReturn(List.of(link));
        when(githubChecker.getLastUpdatedDate(anyString())).thenReturn(OffsetDateTime.parse("2024-01-01T10:00:00Z"));

        scheduler.update();

        verifyNoInteractions(botClient);
        verify(updateTrackedLinkTimeUseCase).execute(link.getId());
    }

    public Link createSampleLink(OffsetDateTime lastUpdated, UUID linkId) {
        return Link.builder()
                .id(linkId)
                .url("https://github.com/user/repo")
                .type(LinkType.GITHUB)
                .lastUpdated(lastUpdated)
                .build();
    }
}
