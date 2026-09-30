package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.configuration.SecurityConfig;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.ScrapperController;
import backend.academy.linktracker.scrapper.infrastructure.api.SubscriptionResult;
import backend.academy.linktracker.scrapper.infrastructure.api.auth.JwtAuthenticationEntryPoint;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.AddLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.mappers.SubscriptionToLinkResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.GetUsersSubscriptionsUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.SubscribeUserUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.UnsubscribeUserUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScrapperController.class)
@Import({ObjectMapper.class, SecurityConfig.class, JwtAuthenticationEntryPoint.class})
@ActiveProfiles("test")
class ScrapperControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubscribeUserUseCase subscribeUseCase;

    @MockitoBean
    private UnsubscribeUserUseCase unsubscribeUseCase;

    @MockitoBean
    private GetUsersSubscriptionsUseCase getSubscriptionsUseCase;

    @MockitoBean
    private SubscriptionToLinkResponse responseMapper;

    @MockitoBean
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        lenient()
                .when(meterRegistry.counter(anyString(), any(String[].class)))
                .thenReturn(new SimpleMeterRegistry().counter("temp"));
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor auth() {
        return jwt().jwt(j -> j.subject(USER_ID.toString()));
    }

    @Test
    void getLinksTest() throws Exception {
        SubscriptionResult result = new SubscriptionResult(
                Subscription.builder().build(),
                Link.builder().url("https://github.com").build(),
                User.builder().id(USER_ID).build());

        when(getSubscriptionsUseCase.execute(USER_ID)).thenReturn(List.of(result));
        when(responseMapper.map(any(), any(), any()))
                .thenReturn(new LinkResponse(USER_ID, URI.create("https://github.com"), List.of()));

        mockMvc.perform(get("/api/links").with(auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links").isArray())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.links[0].url").value("https://github.com"));
    }

    @Test
    void getLinksUnauthorizedTest() throws Exception {
        mockMvc.perform(get("/api/links")).andExpect(status().isUnauthorized());
    }

    @Test
    void addLinkTest() throws Exception {
        AddLinkRequest request = new AddLinkRequest(URI.create("https://github.com/user/repo"), List.of("tag"));
        SubscriptionResult result = new SubscriptionResult(
                Subscription.builder().build(),
                Link.builder().url(request.link().toString()).build(),
                User.builder().id(USER_ID).build());

        when(subscribeUseCase.execute(eq(USER_ID), eq(request.link()), any())).thenReturn(result);
        when(responseMapper.map(any(), any(), any()))
                .thenReturn(new LinkResponse(USER_ID, request.link(), request.tags()));

        mockMvc.perform(post("/api/links")
                        .with(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(request.link().toString()));
    }

    @Test
    void removeLinkTest() throws Exception {
        RemoveLinkRequest request = new RemoveLinkRequest(URI.create("https://github.com/user/repo"));
        SubscriptionResult result = new SubscriptionResult(
                Subscription.builder().build(),
                Link.builder().url(request.link().toString()).build(),
                User.builder().id(USER_ID).build());

        when(unsubscribeUseCase.execute(eq(USER_ID), anyString())).thenReturn(result);
        when(responseMapper.map(any(), any(), any())).thenReturn(new LinkResponse(USER_ID, request.link(), List.of()));

        mockMvc.perform(delete("/api/links")
                        .with(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(request.link().toString()));
    }
}
