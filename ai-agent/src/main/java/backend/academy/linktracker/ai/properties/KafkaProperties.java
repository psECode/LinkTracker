package backend.academy.linktracker.ai.properties;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    private String bootstrapServers;

    @NotBlank
    private String rawUpdatesTopic;

    @NotBlank
    private String processedUpdatesTopic;

    @NotBlank
    private String consumerGroup;

    @NotBlank
    private String schemaRegistryUrl;
}
