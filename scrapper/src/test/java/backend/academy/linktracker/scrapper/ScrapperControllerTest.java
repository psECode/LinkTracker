package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.application.links.usecases.CreateTrackedLinkUseCase;
import backend.academy.linktracker.scrapper.application.links.usecases.ReadLinksByUuidsUseCase;
import backend.academy.linktracker.scrapper.application.links.usecases.ReadTrackedLinkByUrlUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.CreateSubscriptionUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.DeleteSubscriptionUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadAllUsersSubscriptionsUseCase;
import backend.academy.linktracker.scrapper.application.subscriptions.usecases.ReadSubscriptionUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.CreateUserUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.DeleteUserUseCase;
import backend.academy.linktracker.scrapper.application.users.usecases.ReadUserByTgIdUseCase;
import backend.academy.linktracker.scrapper.domain.api.dtos.AddLinkRequest;
import backend.academy.linktracker.scrapper.domain.api.dtos.LinkResponse;
import backend.academy.linktracker.scrapper.domain.api.dtos.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.ScrapperController;
import backend.academy.linktracker.scrapper.infrastructure.api.mappers.SubscriptionToLinkResponse;
import backend.academy.linktracker.scrapper.properties.SchedulerProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScrapperController.class)
@Import(com.fasterxml.jackson.databind.ObjectMapper.class)
class ScrapperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private DeleteUserUseCase deleteUserUseCase;

    @MockitoBean
    private CreateTrackedLinkUseCase createTrackedLinkUseCase;

    @MockitoBean
    private ReadUserByTgIdUseCase readUserByTgIdUseCase;

    @MockitoBean
    private ReadAllUsersSubscriptionsUseCase readAllUsersSubscriptionsUseCase;

    @MockitoBean
    private ReadLinksByUuidsUseCase readLinksByUuidsUseCase;

    @MockitoBean
    private SubscriptionToLinkResponse subscriptionToLinkResponse;

    @MockitoBean
    private ReadTrackedLinkByUrlUseCase readTrackedLinkByUrlUseCase;

    @MockitoBean
    private ReadSubscriptionUseCase readSubscriptionUseCase;

    @MockitoBean
    private DeleteSubscriptionUseCase deleteSubscriptionUseCase;

    @MockitoBean
    private CreateSubscriptionUseCase createSubscriptionUseCase;

    @MockitoBean
    private SchedulerProperties properties;

    private final Long chatId = 12345L;

    @Test
    void registerUserTest() throws Exception {
        mockMvc.perform(post("/tg-chat/{id}", chatId)).andExpect(status().isOk());
        verify(createUserUseCase).execute(any());
    }

    @Test
    void deleteNonExistentUserTest() throws Exception {
        when(readUserByTgIdUseCase.execute(chatId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/tg-chat/{id}", chatId)).andExpect(status().isNotFound());
    }

    @Test
    void getLinksTest() throws Exception {
        User user = User.builder().id(UUID.randomUUID()).chatId(chatId).build();
        when(readUserByTgIdUseCase.execute(chatId)).thenReturn(Optional.of(user));
        when(readAllUsersSubscriptionsUseCase.execute(any())).thenReturn(List.of());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links").isArray())
                .andExpect(jsonPath("$.size").value(0));
    }

    @Test
    void addLinkTest() throws Exception {
        AddLinkRequest request = new AddLinkRequest(URI.create("https://github.com/user/repo"), List.of("tag"));
        User user = User.builder().id(UUID.randomUUID()).chatId(chatId).build();
        Link link = Link.builder()
                .id(UUID.randomUUID())
                .url(request.link().toString())
                .build();
        LinkResponse response = new LinkResponse(chatId, request.link(), request.tags());

        when(readUserByTgIdUseCase.execute(chatId)).thenReturn(Optional.of(user));
        when(properties.getInterval()).thenReturn(Duration.ofMinutes(5));
        when(properties.getLinkCheckInterval()).thenReturn(Duration.ofMinutes(10));
        when(createTrackedLinkUseCase.execute(any())).thenReturn(Optional.of(link));
        when(readSubscriptionUseCase.execute(any())).thenReturn(Optional.empty());
        when(createSubscriptionUseCase.execute(any()))
                .thenReturn(Optional.of(Subscription.builder().build()));
        when(subscriptionToLinkResponse.map(any(), any(), any())).thenReturn(response);

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(request.link().toString()));
    }

    @Test
    void wrongLinkTest() throws Exception {
        AddLinkRequest request = new AddLinkRequest(URI.create("https://google.com"), List.of());
        when(readUserByTgIdUseCase.execute(chatId)).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void linkAlreadyThereTest() throws Exception {
        AddLinkRequest request = new AddLinkRequest(URI.create("https://github.com/user/repo"), List.of());
        User user = User.builder().id(UUID.randomUUID()).build();
        Link link = Link.builder().id(UUID.randomUUID()).build();

        when(readUserByTgIdUseCase.execute(chatId)).thenReturn(Optional.of(user));
        when(createTrackedLinkUseCase.execute(any())).thenReturn(Optional.of(link));
        when(readSubscriptionUseCase.execute(any()))
                .thenReturn(Optional.of(Subscription.builder().build()));

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void removeLinkTest() throws Exception {
        RemoveLinkRequest request = new RemoveLinkRequest(URI.create("https://github.com/user/repo"));
        User user = User.builder().id(UUID.randomUUID()).chatId(chatId).build();
        Link link = Link.builder()
                .id(UUID.randomUUID())
                .url(request.link().toString())
                .build();
        Subscription sub = Subscription.builder().id(UUID.randomUUID()).build();

        when(readUserByTgIdUseCase.execute(chatId)).thenReturn(Optional.of(user));
        when(readTrackedLinkByUrlUseCase.execute(anyString())).thenReturn(Optional.of(link));
        when(readSubscriptionUseCase.execute(any())).thenReturn(Optional.of(sub));

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(deleteSubscriptionUseCase).execute(sub.getId());
    }
}
