package backend.academy.linktracker.scrapper.links;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.application.links.usecases.ReadTrackedLinkService;
import backend.academy.linktracker.scrapper.application.links.usecases.UpdateTrackedLinkTimeUseCase;
import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.domain.links.dtos.UpdateTimeDTO;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTrackedLinkTimeUseCaseTest {

    @Mock
    private ReadTrackedLinkService readTrackedLinkService;

    @Mock
    private LinkRepository linkRepository;

    @InjectMocks
    private UpdateTrackedLinkTimeUseCase useCase;

    private final UUID linkId = UUID.randomUUID();
    private final Duration checkInterval = Duration.ofMinutes(5);

    @Test
    void updateWhenHasUpdatesTest() {
        Link link = Link.builder()
                .id(linkId)
                .lastUpdated(OffsetDateTime.MIN)
                .checkInterval(checkInterval)
                .build();

        when(readTrackedLinkService.readByUUID(linkId)).thenReturn(Optional.of(link));
        UpdateTimeDTO dto = new UpdateTimeDTO(linkId, true);

        useCase.execute(dto);

        verify(linkRepository).updateMetadata(argThat(updateDto -> {
            assertThat(updateDto.id()).isEqualTo(linkId);
            assertThat(updateDto.lastUpdated()).isCloseTo(OffsetDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(updateDto.nextCheckAt()).isAfter(OffsetDateTime.now());
            return true;
        }));
    }

    @Test
    void updateWhenNoUpdatesTest() {
        OffsetDateTime originalLastUpdate = OffsetDateTime.now().minusDays(1);
        Link link = Link.builder()
                .id(linkId)
                .lastUpdated(originalLastUpdate)
                .checkInterval(checkInterval)
                .build();

        when(readTrackedLinkService.readByUUID(linkId)).thenReturn(Optional.of(link));
        UpdateTimeDTO dto = new UpdateTimeDTO(linkId, false);

        useCase.execute(dto);

        verify(linkRepository).updateMetadata(argThat(updateDto -> {
            assertThat(updateDto.lastUpdated()).isEqualTo(originalLastUpdate);
            return true;
        }));
    }
}
