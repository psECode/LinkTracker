package backend.academy.linktracker.scrapper.infrastructure.repository.tags.jpa;

import backend.academy.linktracker.scrapper.domain.tags.entities.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    /**
     * Преобразует JPA Entity в чистую доменную модель Tag через Builder
     */
    public Tag toDomain(TagJpaEntity entity) {
        if (entity == null || entity.getId() == null) {
            return null;
        }

        return Tag.builder()
                .subscriptionId(entity.getId().getSubscriptionId())
                .tagString(entity.getId().getTag())
                .build();
    }

    /**
     * Преобразует доменную модель Tag обратно в JPA Entity
     */
    public TagJpaEntity toEntity(Tag domain) {
        if (domain == null) {
            return null;
        }

        // Создаем составной ключ для сущности
        TagJpaEntity.TagId id = new TagJpaEntity.TagId(domain.getSubscriptionId(), domain.getTagString());

        // Возвращаем новую сущность (предполагаем наличие конструктора)
        return new TagJpaEntity(id);
    }
}
