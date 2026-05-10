package backend.academy.linktracker.scrapper.infrastructure.repository.links.jdbc;

import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.domain.links.LinkType;
import backend.academy.linktracker.scrapper.domain.links.dtos.CreateTrackedLinkDTO;
import backend.academy.linktracker.scrapper.domain.links.dtos.UpdateDateDTO;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jdbc")
@Profile("jdbc")
public class JdbcLinkRepository implements LinkRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final RowMapper<Link> rowMapper = (rs, n) -> Link.builder()
            .id(rs.getObject("id", UUID.class))
            .url(rs.getString("url"))
            .type(LinkType.valueOf(rs.getString("type")))
            .lastUpdated(rs.getObject("last_updated", OffsetDateTime.class))
            .nextCheckAt(rs.getObject("next_check_at", OffsetDateTime.class))
            .checkInterval(Duration.parse(rs.getString("check_interval")))
            .build();

    @Override
    public Optional<Link> readByUrl(String url) {
        return jdbc.query("SELECT * FROM links WHERE url = :u", Map.of("u", url), rowMapper).stream()
                .findFirst();
    }

    @Override
    public Optional<Link> readById(UUID id) {
        return jdbc.query("SELECT * FROM links WHERE id = :id", Map.of("id", id), rowMapper).stream()
                .findFirst();
    }

    @Override
    public List<Link> readReadyToCheck(OffsetDateTime now, int limit) {
        String sql = "SELECT * FROM links WHERE next_check_at < :n ORDER BY next_check_at ASC LIMIT :limit";

        MapSqlParameterSource params =
                new MapSqlParameterSource().addValue("n", now).addValue("limit", limit);

        return jdbc.query(sql, params, rowMapper);
    }

    @Override
    public List<Link> readAllByIds(Set<UUID> ids) {
        return jdbc.query("SELECT * FROM links WHERE id IN (:ids)", Map.of("ids", ids), rowMapper);
    }

    @Override
    public Optional<Link> save(CreateTrackedLinkDTO dto) {
        String sql =
                "INSERT INTO links (id, url, type, last_updated, next_check_at, check_interval) VALUES (:id, :u, :t, :l, :n, :i) ON CONFLICT (url) DO UPDATE SET url=EXCLUDED.url RETURNING *";
        return jdbc
                .query(
                        sql,
                        new MapSqlParameterSource()
                                .addValue("id", UUID.randomUUID())
                                .addValue("u", dto.link())
                                .addValue("t", dto.type().name())
                                .addValue("l", dto.lastCheckAt())
                                .addValue("n", dto.nextCheckAt())
                                .addValue("i", dto.checkInterval().toString()),
                        rowMapper)
                .stream()
                .findFirst();
    }

    @Override
    public void updateMetadata(UpdateDateDTO dto) {
        String sql = "UPDATE links SET last_updated = :last, next_check_at = :next WHERE id = :id";
        jdbc.update(sql, Map.of("last", dto.lastUpdated(), "next", dto.nextCheckAt(), "id", dto.id()));
    }

    @Override
    public Optional<Link> delete(UUID id) {
        return jdbc.query("DELETE FROM links WHERE id = :id RETURNING *", Map.of("id", id), rowMapper).stream()
                .findFirst();
    }
}
