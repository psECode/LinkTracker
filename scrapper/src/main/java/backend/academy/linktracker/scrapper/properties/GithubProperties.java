package backend.academy.linktracker.scrapper.properties;

import java.time.Duration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.github")
@Validated
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class GithubProperties {

    private String token;

    private Duration timeout = Duration.ofSeconds(10);

    int issuesPerOnce;
}
