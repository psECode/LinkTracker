package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.infrastructure.api.InternalTokenInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;
    private final String internalToken;

    public WebMvcConfiguration(ObjectMapper objectMapper, @Value("${app.internal-token}") String internalToken) {
        this.objectMapper = objectMapper;
        this.internalToken = internalToken;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InternalTokenInterceptor(objectMapper, internalToken))
                .addPathPatterns("/updates");
    }
}
