package backend.academy.linktracker.scrapper.infrastructure.repository.tags.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscription_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagJpaEntity {

    @EmbeddedId
    private TagId id;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class TagId implements Serializable {
        @Column(name = "subscription_id")
        private UUID subscriptionId;

        @Column(name = "tag")
        private String tag;
    }
}
