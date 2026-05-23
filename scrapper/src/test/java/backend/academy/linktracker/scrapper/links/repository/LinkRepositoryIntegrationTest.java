package backend.academy.linktracker.scrapper.links.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import backend.academy.linktracker.scrapper.ScrapperApplication;
import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import backend.academy.linktracker.scrapper.application.links.usecases.CreateTrackedLinkService;
import backend.academy.linktracker.scrapper.application.links.usecases.ReadTrackedLinkService;
import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.dtos.CreateTrackedLinkDTO;
import backend.academy.linktracker.scrapper.domain.links.dtos.UpdateDateDTO;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.infrastructure.api.OutboxProcessor;
import backend.academy.linktracker.scrapper.infrastructure.api.updateSenders.LinkUpdateSender;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = ScrapperApplication.class)
@Import(TestcontainersConfiguration.class)
@Transactional
@ActiveProfiles("test")
public abstract class LinkRepositoryIntegrationTest {
    @Autowired
    protected LinkRepository linkRepository;

    @MockitoBean
    protected CreateTrackedLinkService createTrackedLinkService;

    @MockitoBean
    protected ReadTrackedLinkService readTrackedLinkService;

    @MockitoBean
    protected OutboxProcessor outboxProcessor;

    @MockitoBean
    protected LinkUpdateSender linkUpdateSender;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", TestcontainersConfiguration.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", TestcontainersConfiguration.POSTGRES::getUsername);
        registry.add("spring.datasource.password", TestcontainersConfiguration.POSTGRES::getPassword);

        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "file:migrations/changelog-master.xml");

        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("spring.jpa.open-in-view", () -> "false");
    }

    @Test
    void shouldSaveAndFindByUrl() {
        String url = "https://github.com/user/repo";
        CreateTrackedLinkDTO dto = createDto(url);

        linkRepository.save(dto);
        Optional<Link> found = linkRepository.readByUrl(url);

        assertThat(found).isPresent();
        assertThat(found.get().getUrl()).isEqualTo(url);
        assertThat(found.get().getType()).isEqualTo(LinkType.GITHUB);
    }

    @Test
    void shouldFindById() {
        Link saved =
                linkRepository.save(createDto("https://stackoverflow.com/q/1")).get();

        Optional<Link> found = linkRepository.readById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUrl()).isEqualTo(saved.getUrl());
    }

    @Test
    void shouldThrowExceptionOnDuplicateUrl() {
        String url = "https://github.com/test/123";
        CreateTrackedLinkDTO dto = createDto(url);

        Link firstSave = linkRepository.save(dto).orElseThrow();
        UUID firstId = firstSave.getId();

        Link secondSave = linkRepository.save(dto).orElseThrow();
        UUID secondId = secondSave.getId();

        assertThat(firstId).isEqualTo(secondId);

        Optional<Link> found = linkRepository.readByUrl(url);
        assertThat(found).isPresent();
    }

    @Test
    void shouldDeleteLink() {
        Link saved =
                linkRepository.save(createDto("https://github/user/psecode")).get();

        linkRepository.delete(saved.getId());

        assertThat(linkRepository.readById(saved.getId())).isEmpty();
    }

    @Test
    void shouldReadReadyToCheckLinks() {
        OffsetDateTime now = OffsetDateTime.now();

        linkRepository.save(new CreateTrackedLinkDTO(
                "https://expired.com", now.minusMinutes(5), LinkType.GITHUB, now, Duration.ofMinutes(10)));

        linkRepository.save(new CreateTrackedLinkDTO(
                "https://fresh.com", now.plusMinutes(5), LinkType.GITHUB, now, Duration.ofMinutes(10)));

        List<Link> readyLinks = linkRepository.readReadyToCheck(now, 10);

        assertThat(readyLinks).hasSize(1);
        assertThat(readyLinks.getFirst().getUrl()).isEqualTo("https://expired.com");
    }

    @Test
    void shouldUpdateMetadata() {
        Link link = linkRepository.save(createDto("https://should_update.com")).get();
        OffsetDateTime newLastUpdated = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        OffsetDateTime newNextCheck = newLastUpdated.plusMinutes(15);

        UpdateDateDTO updateDto = new UpdateDateDTO(link.getId(), newLastUpdated, newNextCheck);

        linkRepository.updateMetadata(updateDto);
        Link updated = linkRepository.readById(link.getId()).get();

        assertThat(updated.getLastUpdated()).isCloseTo(newLastUpdated, within(1, ChronoUnit.MILLIS));
        assertThat(updated.getNextCheckAt()).isCloseTo(newNextCheck, within(1, ChronoUnit.MILLIS));
    }

    protected CreateTrackedLinkDTO createDto(String url) {
        return new CreateTrackedLinkDTO(
                url,
                OffsetDateTime.now().plusMinutes(5),
                LinkType.GITHUB,
                OffsetDateTime.now().minusDays(1),
                Duration.ofMinutes(5));
    }
}
