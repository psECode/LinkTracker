package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.domain.links.entities.Link;
import backend.academy.linktracker.scrapper.domain.subscriptions.entities.Subscription;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.ScrapperController;
import backend.academy.linktracker.scrapper.infrastructure.api.SubscriptionResult;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.AddLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.LinkResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.UserNotFoundException;
import backend.academy.linktracker.scrapper.infrastructure.api.mappers.SubscriptionToLinkResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.GetUsersSubscriptionsUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.RegisterUserUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.SubscribeUserUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.UnregisterUserUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.UnsubscribeUserUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
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
    private SubscribeUserUseCase subscribeUseCase;

    @MockitoBean
    private UnsubscribeUserUseCase unsubscribeUseCase;

    @MockitoBean
    private GetUsersSubscriptionsUseCase getSubscriptionsUseCase;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private UnregisterUserUseCase unregisterUserUseCase;

    @MockitoBean
    private SubscriptionToLinkResponse responseMapper;

    private final Long chatId = 12345L;

    @Test
    void registerUserTest() throws Exception {
        mockMvc.perform(post("/tg-chat/{id}", chatId))
            .andExpect(status().isOk());

        verify(registerUserUseCase).execute(chatId);
    }

    @Test
    void unregisterUserTest() throws Exception {
        mockMvc.perform(delete("/tg-chat/{id}", chatId))
            .andExpect(status().isOk());

        verify(unregisterUserUseCase).execute(chatId);
    }

    @Test
    void getLinksTest() throws Exception {
        SubscriptionResult result = new SubscriptionResult(
            Subscription.builder().build(),
            Link.builder().url("https://github.com").build(),
            User.builder().chatId(chatId).build());

        when(getSubscriptionsUseCase.execute(chatId)).thenReturn(List.of(result));
        when(responseMapper.map(any(), any(), any()))
            .thenReturn(new LinkResponse(chatId, URI.create("https://github.com"), List.of()));

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.links").isArray())
            .andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.links[0].url").value("https://github.com"));
    }

    @Test
    void addLinkTest() throws Exception {
        AddLinkRequest request = new AddLinkRequest(URI.create("https://github.com/user/repo"), List.of("tag"));
        SubscriptionResult result = new SubscriptionResult(
            Subscription.builder().build(),
            Link.builder().url(request.link().toString()).build(),
            User.builder().chatId(chatId).build());

        when(subscribeUseCase.execute(eq(chatId), eq(request.link()), any()))
            .thenReturn(result);

        when(responseMapper.map(any(), any(), any()))
            .thenReturn(new LinkResponse(chatId, request.link(), request.tags()));

        mockMvc.perform(post("/links")
                .header("Tg-Chat-Id", chatId)
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
            User.builder().chatId(chatId).build());

        when(unsubscribeUseCase.execute(eq(chatId), anyString())).thenReturn(result);

        when(responseMapper.map(any(), any(), any()))
            .thenReturn(new LinkResponse(chatId, request.link(), List.of()));

        mockMvc.perform(delete("/links")
                .header("Tg-Chat-Id", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.url").value(request.link().toString()));
    }

    @Test
    void deleteNonExistentUserTest() throws Exception {
        doThrow(new UserNotFoundException(chatId)).when(unregisterUserUseCase).execute(chatId);

        mockMvc.perform(delete("/tg-chat/{id}", chatId))
            .andExpect(status().isNotFound());
    }
}
