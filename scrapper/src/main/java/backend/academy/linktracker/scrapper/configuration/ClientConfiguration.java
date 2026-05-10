package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.domain.api.BotClient;
import backend.academy.linktracker.scrapper.domain.api.github.GithubClient;
import backend.academy.linktracker.scrapper.domain.api.stackoverflow.StackoverflowClient;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ClientConfiguration {

    private final GithubProperties githubProperties;
    private final StackoverflowProperties stackoverflowProperties;

    @Bean
    public BotClient botClient(@Value("${app.bot.url}") String baseUrl) {
        RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();

        return createClient(BotClient.class, restClient);
    }

    @Bean
    public GithubClient githubClient(@Value("${app.github.url:https://api.github.com}") String baseUrl) {
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "LinkTrackerBot")
                .defaultHeader("Authorization", "Bearer " + githubProperties.getToken())
                .build();

        return createClient(GithubClient.class, restClient);
    }

    @Bean
    public StackoverflowClient stackoverflowClient(
            @Value("${app.stackoverflow.url:https://api.stackexchange.com/2.3/}") String baseUrl) {

        UriComponentsBuilder uriBuilder =
                UriComponentsBuilder.fromUriString(baseUrl).queryParam("site", "stackoverflow");

        String key = stackoverflowProperties.getKey();
        if (key != null) {
            uriBuilder.queryParam("key", key);
        }

        String token = stackoverflowProperties.getAccessToken();
        if (token != null) {
            uriBuilder.queryParam("access_token", token);
        }

        RestClient restClient = RestClient.builder()
                .baseUrl(uriBuilder.build().toUriString())
                .defaultHeader("User-Agent", "LinkTrackerBot")
                .requestInterceptor((request, body, execution) -> {
                    log.info("Requesting StackExchange API: {}", request.getURI());
                    return execution.execute(request, body);
                })
                .build();

        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(StackoverflowClient.class);
    }

    private <T> T createClient(Class<T> clientClass, RestClient restClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(clientClass);
    }
}
