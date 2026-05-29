package backend.academy.linktracker.scrapper.infrastructure.repository.tags.jpa;

import backend.academy.linktracker.scrapper.domain.tags.entities.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    public Tag toDomain(TagJpaEntity entity) {
        if (entity == null || entity.getId() == null) {
            return null;
        }

        return Tag.builder()
                .subscriptionId(entity.getId().getSubscriptionId())
                .tagString(entity.getId().getTag())
                .build();
    }

    public TagJpaEntity toEntity(Tag domain) {
        if (domain == null) {
            return null;
        }

        TagJpaEntity.TagId id = new TagJpaEntity.TagId(domain.getSubscriptionId(), domain.getTagString());

        return new TagJpaEntity(id);
    }
}
