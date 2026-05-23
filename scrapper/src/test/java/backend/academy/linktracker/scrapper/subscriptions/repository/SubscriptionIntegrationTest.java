package backend.academy.linktracker.scrapper.subscriptions.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.ScrapperApplication;
import backend.academy.linktracker.scrapper.TestcontainersConfiguration;
import backend.academy.linktracker.scrapper.domain.links.LinkRepository;
import backend.academy.linktracker.scrapper.domain.subscriptions.SubscriptionRepository;
import backend.academy.linktracker.scrapper.domain.subscriptions.dtos.ReadSubscriptionDTO;
import backend.academy.linktracker.scrapper.domain.tags.TagRepository;
import backend.academy.linktracker.scrapper.domain.tags.entities.Tag;
import backend.academy.linktracker.scrapper.domain.users.UsersRepository;
import backend.academy.linktracker.scrapper.domain.users.entities.User;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.AddLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.updateSenders.LinkUpdateSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ScrapperApplication.class)
@Import({TestcontainersConfiguration.class, com.fasterxml.jackson.databind.ObjectMapper.class})
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public abstract class SubscriptionIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected LinkRepository linkRepository;

    @Autowired
    protected UsersRepository userRepository;

    @Autowired
    protected SubscriptionRepository subscriptionRepository;

    @Autowired
    protected TagRepository tagRepository;

    @MockitoBean
    protected LinkUpdateSender linkUpdateSender;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", TestcontainersConfiguration.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", TestcontainersConfiguration.POSTGRES::getUsername);
        registry.add("spring.datasource.password", TestcontainersConfiguration.POSTGRES::getPassword);

        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "file:migrations/changelog-master.xml");

        registry.add("spring.sql.init.mode", () -> "never");
        registry.add("spring.jpa.open-in-view", () -> "false");
    }

    @Test
    void SubscriptionTest() throws Exception {
        // g
        Long chatId = 123321L;
        URI url = URI.create("https://stackoverflow.com/questions/1");
        List<String> tags = List.of("java", "good");

        mockMvc.perform(post("/tg-chat/{id}", chatId)).andExpect(status().isOk());

        AddLinkRequest request = new AddLinkRequest(url, tags);

        // w
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // t
        var user = userRepository.readByChatId(chatId).orElseThrow();

        var link = linkRepository.readByUrl(url.toString()).orElseThrow();
        assertThat(link.getUrl()).isEqualTo(url.toString());

        var subscription = subscriptionRepository
                .read(new ReadSubscriptionDTO(user.getId(), link.getId()))
                .orElseThrow();

        var savedTags = tagRepository.readBySubscription(subscription.getId());

        assertThat(savedTags).hasSize(2).extracting(Tag::getTagString).containsExactlyInAnyOrder("java", "good");
    }

    @Test
    void DeletingSubscriptionsWhenUserDeletesTest() throws Exception {
        // g
        Long chatId = 111L;
        String url = "https://github.com/user/repo";
        setupUserAndSubscription(chatId, url, List.of("java"));

        RemoveLinkRequest removeRequest = new RemoveLinkRequest(URI.create(url));

        // w
        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeRequest)))
                .andExpect(status().isOk());

        // t
        var user = userRepository.readByChatId(chatId).orElseThrow();
        var link = linkRepository.readByUrl(url).orElseThrow();

        assertThat(subscriptionRepository.read(new ReadSubscriptionDTO(user.getId(), link.getId())))
                .isEmpty();
        assertThat(tagRepository.readBySubscription(user.getId())).isEmpty();
    }

    @Test
    void GetTagsTest() throws Exception {
        Long chatId = 222L;
        String url = "https://stackoverflow.com/questions/1";
        List<String> tags = List.of("spring", "hibernate");
        setupUserAndSubscription(chatId, url, tags);

        // w & t
        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].url").value(url))
                .andExpect(jsonPath("$.links[0].tags").isArray())
                .andExpect(jsonPath("$.links[0].tags", hasSize(2)))
                .andExpect(jsonPath("$.links[0].tags", containsInAnyOrder("spring", "hibernate")));
    }

    @Test
    void DeletingUserTest() throws Exception {
        // g
        Long chatId = 333L;
        setupUserAndSubscription(chatId, "https://github.com/1", List.of("t1"));
        User user = userRepository.readByChatId(chatId).orElseThrow();

        // w
        mockMvc.perform(delete("/tg-chat/{id}", chatId)).andExpect(status().isOk());

        // t
        assertThat(userRepository.readByChatId(chatId)).isEmpty();
        assertThat(subscriptionRepository.readByUser(user.getId())).isEmpty();
    }

    @Test
    void SubscribingWithoutRegistrationTest() throws Exception {
        Long unknownChatId = 999999L;
        AddLinkRequest request = new AddLinkRequest(URI.create("https://github.com"), List.of());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", unknownChatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void DeletingNonExistentUserTest() throws Exception {
        mockMvc.perform(delete("/tg-chat/{id}", 888888L)).andExpect(status().isNotFound());
    }

    @Test
    void DeletingNonExistingSubscriptionTest() throws Exception {
        Long chatId = 444L;
        mockMvc.perform(post("/tg-chat/{id}", chatId));

        RemoveLinkRequest request = new RemoveLinkRequest(URI.create("https://no-sub.com"));

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    protected void setupUserAndSubscription(Long chatId, String url, List<String> tags) throws Exception {
        mockMvc.perform(post("/tg-chat/{id}", chatId));
        AddLinkRequest request = new AddLinkRequest(URI.create(url), tags);
        mockMvc.perform(post("/links")
                .header("Tg-Chat-Id", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
