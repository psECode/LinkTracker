package backend.academy.linktracker.scrapper.infrastructure.repository.links.jpa;

import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.domain.links.dtos.CreateTrackedLinkDTO;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jpa")
public class JpaLinkRepository implements LinkRepository {
    private final LinkJpaRepositoryInterface jpa;
    private final LinkMapper mapper;

    @Override
    public Optional<Link> readById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Link> readByUrl(String url) {
        return jpa.findByUrl(url).map(mapper::toDomain);
    }

    @Override
    public List<Link> readReadyToCheck(OffsetDateTime now) {
        return jpa.findAllByNextCheckAtBefore(now).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Link> readAllByIds(Set<UUID> ids) {
        return jpa.findAllByIdIn(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public Optional<Link> save(CreateTrackedLinkDTO dto) {
        return jpa.findByUrl(dto.link()).map(mapper::toDomain).or(() -> {
            LinkJpaEntity entity = new LinkJpaEntity(
                    UUID.randomUUID(),
                    dto.link(),
                    dto.type(),
                    dto.lastCheckAt(),
                    dto.nextCheckAt(),
                    dto.checkInterval());
            return Optional.of(mapper.toDomain(jpa.save(entity)));
        });
    }

    @Override
    @Transactional
    public Optional<Link> delete(UUID id) {
        return jpa.findById(id).map(e -> {
            jpa.delete(e);
            return mapper.toDomain(e);
        });
    }
}
