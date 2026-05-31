package backend.academy.linktracker.scrapper.properties;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.kafka")
@Validated
@Getter
@Setter
@NoArgsConstructor
public class KafkaProperties {
    @NotNull
    private String bootstrapServers;

    @NotNull
    private String topicName;

    private String schemaRegistryUrl;

    private boolean useQueue = true;
}
