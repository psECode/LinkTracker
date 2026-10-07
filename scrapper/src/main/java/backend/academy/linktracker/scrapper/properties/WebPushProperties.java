package backend.academy.linktracker.scrapper.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.web-push")
public class WebPushProperties {

    private String publicKey = "";

    private String privateKey = "";

    private String subject = "mailto:link-tracker@example.com";

    public boolean configured() {
        return publicKey != null && !publicKey.isBlank() && privateKey != null && !privateKey.isBlank();
    }
}
