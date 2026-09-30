package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserService;
import backend.academy.linktracker.scrapper.application.users.usecases.RegisterTelegramUserUseCase;
import backend.academy.linktracker.scrapper.configuration.SecurityConfig;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.InternalController;
import backend.academy.linktracker.scrapper.infrastructure.api.SubscriptionResult;
import backend.academy.linktracker.scrapper.infrastructure.api.auth.JwtAuthenticationEntryPoint;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.AddLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.mappers.SubscriptionToLinkResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.GetUsersSubscriptionsUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.SubscribeUserUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.UnregisterUserUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.UnsubscribeUserUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalController.class)
@Import({ObjectMapper.class, SecurityConfig.class, JwtAuthenticationEntryPoint.class})
@ActiveProfiles("test")
class InternalControllerTest {

    private static final Long CHAT_ID = 12345L;
    private static final String INTERNAL_TOKEN = "test-internal-token";
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterTelegramUserUseCase registerTelegramUserUseCase;

    @MockitoBean
    private UnregisterUserUseCase unregisterUserUseCase;

    @MockitoBean
    private GetUsersSubscriptionsUseCase getSubscriptionsUseCase;

    @MockitoBean
    private SubscribeUserUseCase subscribeUseCase;

    @MockitoBean
    private UnsubscribeUserUseCase unsubscribeUseCase;

    @MockitoBean
    private ReadUserService readUserService;

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

    @Test
    void registerTelegramUserTest() throws Exception {
        mockMvc.perform(post("/internal/tg-chat/{id}", CHAT_ID).header("X-Internal-Token", INTERNAL_TOKEN))
                .andExpect(status().isOk());

        verify(registerTelegramUserUseCase).execute(CHAT_ID);
    }

    @Test
    void registerWithoutInternalTokenTest() throws Exception {
        mockMvc.perform(post("/internal/tg-chat/{id}", CHAT_ID)).andExpect(status().isUnauthorized());
    }

    @Test
    void registerWithWrongInternalTokenTest() throws Exception {
        mockMvc.perform(post("/internal/tg-chat/{id}", CHAT_ID).header("X-Internal-Token", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unregisterTelegramUserTest() throws Exception {
        when(readUserService.readByTelegramId(CHAT_ID))
                .thenReturn(Optional.of(
                        User.builder().id(USER_ID).telegramId(CHAT_ID).build()));

        mockMvc.perform(delete("/internal/tg-chat/{id}", CHAT_ID).header("X-Internal-Token", INTERNAL_TOKEN))
                .andExpect(status().isOk());

        verify(unregisterUserUseCase).execute(USER_ID);
    }

    @Test
    void unregisterUnknownChatTest() throws Exception {
        when(readUserService.readByTelegramId(CHAT_ID)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/internal/tg-chat/{id}", CHAT_ID).header("X-Internal-Token", INTERNAL_TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLinksTest() throws Exception {
        SubscriptionResult result = new SubscriptionResult(
                Subscription.builder().build(),
                Link.builder().url("https://github.com").build(),
                User.builder().id(USER_ID).telegramId(CHAT_ID).build());

        when(readUserService.readByTelegramId(CHAT_ID))
                .thenReturn(Optional.of(
                        User.builder().id(USER_ID).telegramId(CHAT_ID).build()));
        when(getSubscriptionsUseCase.execute(USER_ID)).thenReturn(List.of(result));
        when(responseMapper.map(any(), any(), any()))
                .thenReturn(new LinkResponse(USER_ID, URI.create("https://github.com"), List.of()));

        mockMvc.perform(get("/internal/links")
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("Tg-Chat-Id", CHAT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].url").value("https://github.com"));
    }

    @Test
    void addLinkTest() throws Exception {
        AddLinkRequest request = new AddLinkRequest(URI.create("https://github.com/user/repo"), List.of("tag"));
        SubscriptionResult result = new SubscriptionResult(
                Subscription.builder().build(),
                Link.builder().url(request.link().toString()).build(),
                User.builder().id(USER_ID).telegramId(CHAT_ID).build());

        when(readUserService.readByTelegramId(CHAT_ID))
                .thenReturn(Optional.of(
                        User.builder().id(USER_ID).telegramId(CHAT_ID).build()));
        when(subscribeUseCase.execute(eq(USER_ID), eq(request.link()), any())).thenReturn(result);
        when(responseMapper.map(any(), any(), any()))
                .thenReturn(new LinkResponse(USER_ID, request.link(), request.tags()));

        mockMvc.perform(post("/internal/links")
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("Tg-Chat-Id", CHAT_ID)
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
                User.builder().id(USER_ID).telegramId(CHAT_ID).build());

        when(readUserService.readByTelegramId(CHAT_ID))
                .thenReturn(Optional.of(
                        User.builder().id(USER_ID).telegramId(CHAT_ID).build()));
        when(unsubscribeUseCase.execute(eq(USER_ID), anyString())).thenReturn(result);
        when(responseMapper.map(any(), any(), any())).thenReturn(new LinkResponse(USER_ID, request.link(), List.of()));

        mockMvc.perform(delete("/internal/links")
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("Tg-Chat-Id", CHAT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(request.link().toString()));
    }
}
