package backend.academy.linktracker.scrapper.application.links.usecases;

import backend.academy.linktracker.scrapper.domain.links.dtos.CreateTrackedLinkDTO;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateTrackedLinkTimeUseCase {
    private final ReadTrackedLinkByUuidUseCase readTrackedLinkByUuidUseCase;
    private final CreateTrackedLinkUseCase createTrackedLinkUseCase;

    public void execute(UUID linkId) {
        readTrackedLinkByUuidUseCase.execute(linkId).ifPresent(link -> {
            OffsetDateTime nextCheck = OffsetDateTime.now().plus(link.getCheckInterval());

            link.setNextCheckAt(nextCheck);

            CreateTrackedLinkDTO dto = new CreateTrackedLinkDTO(
                    link.getUrl(),
                    link.getNextCheckAt(),
                    link.getType(),
                    link.getLastUpdated(),
                    link.getCheckInterval());

            createTrackedLinkUseCase.execute(dto);
        });
    }
}
