package backend.academy.linktracker.bot.properties;

import java.time.Duration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.cache")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class CacheProperties {
    private Duration listLinksTtl;
    private Duration contextTtl;
}
