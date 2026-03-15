package backend.academy.linktracker.bot.test.bot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.bot.application.bot.usecases.ProcessUpdateUseCase;
import backend.academy.linktracker.bot.domain.api.dtos.LinkUpdate;
import backend.academy.linktracker.bot.infrastructure.api.BotController;
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

@WebMvcTest(BotController.class)
@Import(com.fasterxml.jackson.databind.ObjectMapper.class)
class BotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcessUpdateUseCase processUpdateUseCase;

    @Test
    void sendUpdate_Success() throws Exception {
        LinkUpdate update = new LinkUpdate(1L, URI.create("https://github.com"), "abracadabra", List.of(123L));

        mockMvc.perform(post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());

        verify(processUpdateUseCase).execute(any());
    }
}
