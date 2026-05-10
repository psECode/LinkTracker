package backend.academy.linktracker.scrapper.links.repository;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.dtos.CreateTrackedLinkDTO;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.infrastructure.mocks.links.MemoryLinkRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemoryLinkRepositoryTest {

    private MemoryLinkRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MemoryLinkRepository();
    }

    @Test
    void saveAndReadTest() {
        var dto = createSampleDto("https://example.com");

        Optional<Link> saved = repository.save(dto);

        assertThat(saved).isPresent();
        Link link = saved.get();
        assertThat(link.getId()).isNotNull();
        assertThat(link.getUrl()).isEqualTo(dto.link());
        assertThat(link.getType()).isEqualTo(dto.type());
        assertThat(link.getLastUpdated()).isEqualTo(dto.lastCheckAt());
        assertThat(link.getCheckInterval()).isEqualTo(dto.checkInterval());
        assertThat(link.getNextCheckAt()).isEqualTo(dto.nextCheckAt());

        Optional<Link> byId = repository.readById(link.getId());
        assertThat(byId).contains(link);
    }

    @Test
    void deleteTest() {
        var dto = createSampleDto("https://example.com");
        Link link = repository.save(dto).orElseThrow();
        UUID id = link.getId();

        Optional<Link> deleted = repository.delete(id);

        assertThat(deleted).contains(link);
        assertThat(repository.readById(id)).isEmpty();
    }

    @Test
    void deleteNonExistentTest() {
        Optional<Link> deleted = repository.delete(UUID.randomUUID());

        assertThat(deleted).isEmpty();
    }

    @Test
    void readByIdTest() {
        Link link = repository.save(createSampleDto("https://example.com")).orElseThrow();

        Optional<Link> found = repository.readById(link.getId());

        assertThat(found).contains(link);

        Optional<Link> notFound = repository.readById(UUID.randomUUID());

        assertThat(notFound).isEmpty();
    }

    @Test
    void readByUrlTest() {
        Link link = repository.save(createSampleDto("https://example.com")).orElseThrow();

        Optional<Link> found = repository.readByUrl(link.getUrl());

        assertThat(found).contains(link);

        Optional<Link> notFound = repository.readByUrl("https://unknown.com");

        assertThat(notFound).isEmpty();
    }

    @Test
    void readReadyToCheckTest() {
        OffsetDateTime now = OffsetDateTime.now();
        Link link1 =
                repository.save(createDtoWithNextCheck(now.minusMinutes(5))).orElseThrow();
        Link link2 =
                repository.save(createDtoWithNextCheck(now.minusSeconds(10))).orElseThrow();
        Link link3 = repository.save(createDtoWithNextCheck(now.plusMinutes(5))).orElseThrow();

        List<Link> ready = repository.readReadyToCheck(now);

        assertThat(ready).hasSize(2).containsExactlyInAnyOrder(link1, link2);
    }

    @Test
    void readAllByIdsTest() {
        Link link1 = repository.save(createSampleDto("https://example.com")).orElseThrow();
        Link link2 = repository.save(createSampleDto("https://example2.com")).orElseThrow();
        Link link3 = repository.save(createSampleDto("https://example3.com")).orElseThrow();

        Set<UUID> ids = Set.of(link1.getId(), link2.getId());

        List<Link> result = repository.readAllByIds(ids);

        assertThat(result).hasSize(2).containsExactlyInAnyOrder(link1, link2);
    }

    @Test
    void readAllByIdsEmptyTest() {
        repository.save(createSampleDto("https://example.com"));

        List<Link> result = repository.readAllByIds(Set.of(UUID.randomUUID()));

        assertThat(result).isEmpty();
    }

    private CreateTrackedLinkDTO createSampleDto(String link) {
        return new CreateTrackedLinkDTO(
                link,
                OffsetDateTime.now(),
                LinkType.GITHUB,
                OffsetDateTime.now().plusMinutes(10),
                Duration.ofMinutes(10));
    }

    private CreateTrackedLinkDTO createDtoWithNextCheck(OffsetDateTime nextCheck) {
        return new CreateTrackedLinkDTO(
                "https://example.com/" + UUID.randomUUID(),
                nextCheck,
                LinkType.GITHUB,
                OffsetDateTime.now(),
                Duration.ofMinutes(10));
    }
}
