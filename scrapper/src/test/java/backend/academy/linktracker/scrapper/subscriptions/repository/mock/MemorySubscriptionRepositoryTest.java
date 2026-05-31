package backend.academy.linktracker.scrapper.subscriptions.repository.mock;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.CreateSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.ReadSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import backend.academy.linktracker.scrapper.infrastructure.mocks.subscriptions.MemorySubscriptionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MemorySubscriptionRepositoryTest {

    private MemorySubscriptionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MemorySubscriptionRepository();
    }

    @Test
    void saveTest() {
        UUID userId = UUID.randomUUID();
        UUID linkId = UUID.randomUUID();
        List<String> tags = List.of("java", "spring");
        var dto = new CreateSubscriptionDTO(userId, linkId, tags);

        Optional<Subscription> saved = repository.save(dto);

        assertThat(saved).isPresent();
        Subscription subscription = saved.get();
        assertThat(subscription.getId()).isNotNull();
        assertThat(subscription.getUserId()).isEqualTo(userId);
        assertThat(subscription.getLinkId()).isEqualTo(linkId);
        assertThat(subscription.getTags()).isEqualTo(tags);
    }

    @Test
    void readTest() {
        UUID userId = UUID.randomUUID();
        UUID linkId = UUID.randomUUID();
        var dto = new CreateSubscriptionDTO(userId, linkId, List.of("a"));
        Subscription sub = repository.save(dto).orElseThrow();

        var readDto = new ReadSubscriptionDTO(userId, linkId);

        Optional<Subscription> found = repository.read(readDto);

        assertThat(found).contains(sub);

        var readDtoNotFound = new ReadSubscriptionDTO(UUID.randomUUID(), UUID.randomUUID());

        Optional<Subscription> notFound = repository.read(readDtoNotFound);

        assertThat(notFound).isEmpty();
    }

    @Test
    void readByUser() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID user3 = UUID.randomUUID();

        Subscription sub1 = repository
                .save(new CreateSubscriptionDTO(user1, UUID.randomUUID(), List.of()))
                .orElseThrow();
        Subscription sub2 = repository
                .save(new CreateSubscriptionDTO(user1, UUID.randomUUID(), List.of()))
                .orElseThrow();
        Subscription sub3 = repository
                .save(new CreateSubscriptionDTO(user2, UUID.randomUUID(), List.of()))
                .orElseThrow();

        List<Subscription> forUser1 = repository.readByUser(user1);

        assertThat(forUser1).hasSize(2).containsExactlyInAnyOrder(sub1, sub2);

        List<Subscription> forUser3 = repository.readByUser(user3);

        assertThat(forUser3).isEmpty();
    }

    @Test
    void readByLink() {
        UUID link1 = UUID.randomUUID();
        UUID link2 = UUID.randomUUID();

        Subscription sub1 = repository
                .save(new CreateSubscriptionDTO(UUID.randomUUID(), link1, List.of()))
                .orElseThrow();
        Subscription sub2 = repository
                .save(new CreateSubscriptionDTO(UUID.randomUUID(), link1, List.of()))
                .orElseThrow();
        Subscription sub3 = repository
                .save(new CreateSubscriptionDTO(UUID.randomUUID(), link2, List.of()))
                .orElseThrow();

        List<Subscription> forLink1 = repository.readByLink(link1);

        assertThat(forLink1).hasSize(2).containsExactlyInAnyOrder(sub1, sub2);
    }

    @Test
    void deleteTest() {
        Subscription sub = repository
                .save(new CreateSubscriptionDTO(UUID.randomUUID(), UUID.randomUUID(), List.of()))
                .orElseThrow();
        UUID id = sub.getId();

        repository.delete(id);

        assertThat(repository.read(new ReadSubscriptionDTO(sub.getUserId(), sub.getLinkId())))
                .isEmpty();
    }

    @Test
    void deleteNonExistent() {
        Subscription sub = repository
                .save(new CreateSubscriptionDTO(UUID.randomUUID(), UUID.randomUUID(), List.of()))
                .orElseThrow();

        repository.delete(UUID.randomUUID());

        assertThat(repository.read(new ReadSubscriptionDTO(sub.getUserId(), sub.getLinkId())))
                .isPresent();
    }
}
