package backend.academy.linktracker.scrapper.infrastructure.repository.links.jpa;

import backend.academy.linktracker.scrapper.domain.links.LinkType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LinkJpaEntity {
    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String url;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LinkType type;

    private OffsetDateTime lastUpdated;
    private OffsetDateTime nextCheckAt;

    @Column(nullable = false)
    @Convert(converter = DurationConverter.class)
    private Duration checkInterval;
}
