package backend.academy.linktracker.scrapper.application.links.usecases;

import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.domain.links.dtos.UpdateDateDTO;
import backend.academy.linktracker.scrapper.domain.links.dtos.UpdateTimeDTO;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateTrackedLinkTimeUseCase {
    private final ReadTrackedLinkService readTrackedLinkService;
    private final LinkRepository linkRepository;

    @Transactional
    public void execute(UpdateTimeDTO dto) {
        readTrackedLinkService.readByUUID(dto.linkId()).ifPresent(link -> {
            OffsetDateTime nextCheck = OffsetDateTime.now().plus(link.getCheckInterval());

            link.setNextCheckAt(nextCheck);
            UpdateDateDTO updateDto;
            if (Boolean.TRUE.equals(dto.updated())) {
                updateDto = new UpdateDateDTO(link.getId(), OffsetDateTime.now(), nextCheck);
            } else {
                updateDto = new UpdateDateDTO(link.getId(), link.getLastUpdated(), nextCheck);
            }

            linkRepository.updateMetadata(updateDto);
        });
    }
}
