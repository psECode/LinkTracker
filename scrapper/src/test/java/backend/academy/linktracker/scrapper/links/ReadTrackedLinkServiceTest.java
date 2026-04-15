package backend.academy.linktracker.scrapper.links;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.scrapper.application.links.usecases.ReadTrackedLinkService;
import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadTrackedLinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @InjectMocks
    private ReadTrackedLinkService readService;

    @Test
    void readByUUIDsTest() {
        Set<UUID> ids = Set.of(UUID.randomUUID(), UUID.randomUUID());
        readService.readByUUIDs(ids);
        verify(linkRepository).readAllByIds(ids);
    }

    @Test
    void readByUrlTest() {
        String url = "https://github.com/user/repo";
        readService.readByUrl(url);
        verify(linkRepository).readByUrl(url);
    }

    @Test
    void readNyUUIDTest() {
        UUID id = UUID.randomUUID();
        readService.readByUUID(id);
        verify(linkRepository).readById(id);
    }

    @Test
    void readExpiredLinksTest() {
        int limit = 10;

        readService.readExpiredLinks(limit);

        verify(linkRepository)
                .readReadyToCheck(
                        argThat(time -> time != null
                                && time.isBefore(OffsetDateTime.now().plusSeconds(1))),
                        eq(limit));
    }
}
