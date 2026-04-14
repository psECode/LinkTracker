package backend.academy.linktracker.scrapper.infrastructure.repository.tags.jpa;

import backend.academy.linktracker.scrapper.domain.tags.TagRepository;
import backend.academy.linktracker.scrapper.domain.tags.dtos.CreateTagDto;
import backend.academy.linktracker.scrapper.domain.tags.dtos.DeleteTagDto;
import backend.academy.linktracker.scrapper.domain.tags.dtos.ReadTagDto;
import backend.academy.linktracker.scrapper.domain.tags.entities.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jpa")
public class JpaTagRepository implements TagRepository {

    private final TagJpaRepositoryInterface jpaRepository;
    private final TagMapper tagMapper;

    @Override
    public Optional<Tag> add(CreateTagDto dto) {
        var id = new TagJpaEntity.TagId(dto.subscriptionId(), dto.tag());

        if (jpaRepository.existsById(id)) {
            return Optional.empty();
        }

        TagJpaEntity saved = jpaRepository.save(new TagJpaEntity(id));

        return Optional.of(tagMapper.toDomain(saved));
    }

    @Override
    public List<Tag> readBySubscription(UUID subscriptionId) {
        return jpaRepository.findAllById_SubscriptionId(subscriptionId).stream()
                .map(tagMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tag> readBySubscriptionNTag(ReadTagDto dto) {
        var id = new TagJpaEntity.TagId(dto.subscriptionId(), dto.tag());

        return jpaRepository.findById(id).map(tagMapper::toDomain);
    }

    @Override
    public Optional<Tag> delete(DeleteTagDto dto) {
        var id = new TagJpaEntity.TagId(dto.subscriptionId(), dto.tag());

        return jpaRepository.findById(id).map(entity -> {
            jpaRepository.delete(entity);
            return tagMapper.toDomain(entity);
        });
    }
}
