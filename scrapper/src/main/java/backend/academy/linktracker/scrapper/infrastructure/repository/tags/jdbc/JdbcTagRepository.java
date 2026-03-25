package backend.academy.linktracker.scrapper.infrastructure.repository.tags.jdbc;

import backend.academy.linktracker.scrapper.domain.tags.TagRepository;
import backend.academy.linktracker.scrapper.domain.tags.dtos.CreateTagDto;
import backend.academy.linktracker.scrapper.domain.tags.dtos.DeleteTagDto;
import backend.academy.linktracker.scrapper.domain.tags.dtos.ReadTagDto;
import backend.academy.linktracker.scrapper.domain.tags.entities.Tag;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jdbc")
public class JdbcTagRepository implements TagRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Tag> tagRowMapper = (rs, rowNum) -> Tag.builder()
            .subscriptionId(rs.getObject("subscription_id", UUID.class))
            .tagString(rs.getString("tag"))
            .build();

    @Override
    public Optional<Tag> add(CreateTagDto dto) {
        String sql = "INSERT INTO subscription_tags (subscription_id, tag) "
                + "VALUES (:subId, :tag) ON CONFLICT DO NOTHING RETURNING *";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("subId", dto.subscriptionId())
                .addValue("tag", dto.tag());

        List<Tag> result = jdbcTemplate.query(sql, params, tagRowMapper);
        return result.stream().findFirst();
    }

    @Override
    public List<Tag> readBySubscription(UUID subscriptionId) {
        String sql = "SELECT subscription_id, tag FROM subscription_tags WHERE subscription_id = :subId";

        return jdbcTemplate.query(sql, Map.of("subId", subscriptionId), tagRowMapper);
    }

    @Override
    public Optional<Tag> readBySubscriptionNTag(ReadTagDto dto) {
        String sql = "SELECT * FROM subscription_tags WHERE subscription_id = :subId AND tag = :tag";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("subId", dto.subscriptionId())
                .addValue("tag", dto.tag());

        List<Tag> result = jdbcTemplate.query(sql, params, tagRowMapper);
        return result.stream().findFirst();
    }

    @Override
    public Optional<Tag> delete(DeleteTagDto dto) {
        String sql = "DELETE FROM subscription_tags WHERE subscription_id = :subId AND tag = :tag RETURNING *";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("subId", dto.subscriptionId())
                .addValue("tag", dto.tag());

        List<Tag> result = jdbcTemplate.query(sql, params, tagRowMapper);
        return result.stream().findFirst();
    }

    // @Override
    // @Transactional
    // public List<Optional<Tag>> deleteAllBySubscription(UUID subscriptionId) {
    //    List<Tag> tags = readBySubscription(subscriptionId);
    //
    //    String sql = "DELETE FROM subscription_tags WHERE subscription_id = :subId";
    //    jdbcTemplate.update(sql, Map.of("subId", subscriptionId));
    //
    //     return tags.stream()
    //        .map(t -> Optional.of(new Tag(subscriptionId, t)))
    //        .toList();
    // }
}
