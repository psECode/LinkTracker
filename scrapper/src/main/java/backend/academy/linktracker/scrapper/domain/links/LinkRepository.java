package backend.academy.linktracker.scrapper.domain.links;

import backend.academy.linktracker.scrapper.domain.links.dtos.CreateTrackedLinkDTO;
import backend.academy.linktracker.scrapper.domain.links.dtos.UpdateDateDTO;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface LinkRepository {

    Optional<Link> save(CreateTrackedLinkDTO dto);

    Optional<Link> delete(UUID id);

    Optional<Link> readById(UUID id);

    List<Link> readReadyToCheck(OffsetDateTime now, int limit);

    List<Link> readAllByIds(Set<UUID> ids);

    Optional<Link> readByUrl(String url);

    void updateMetadata(UpdateDateDTO dto);
}
