package backend.academy.linktracker.scrapper.infrastructure.mocks.subscriptions;

import backend.academy.linktracker.scrapper.domain.subscriptions.SubscriptionRepository;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.CreateSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.ReadSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class MemorySubscriptionRepository implements SubscriptionRepository {

    private final Map<UUID, Subscription> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<Subscription> save(CreateSubscriptionDTO dto) {
        ReadSubscriptionDTO readDto = new ReadSubscriptionDTO(dto.userId(), dto.linkId());
        Optional<Subscription> dataSubscription = read(readDto);
        if (dataSubscription.isEmpty()) {
            Subscription subscription = Subscription.builder()
                    .id(UUID.randomUUID())
                    .userId(dto.userId())
                    .tags(dto.tags())
                    .linkId(dto.linkId())
                    .build();
            storage.put(subscription.getId(), subscription);
            return Optional.of(subscription);
        }
        return dataSubscription;
    }

    @Override
    public Optional<Subscription> read(ReadSubscriptionDTO dto) {
        return storage.values().stream()
                .filter(u -> u.getUserId().equals(dto.userId()) && u.getLinkId().equals(dto.linkId()))
                .findFirst();
    }

    @Override
    public List<Subscription> readByUser(UUID chatId) {
        return storage.values().stream()
                .filter(u -> u.getUserId().equals(chatId))
                .toList();
    }

    @Override
    public List<Subscription> readByLink(UUID linkId) {
        return storage.values().stream()
                .filter(u -> u.getLinkId().equals(linkId))
                .toList();
    }

    @Override
    public void delete(UUID id) {
        storage.remove(id);
    }
}
