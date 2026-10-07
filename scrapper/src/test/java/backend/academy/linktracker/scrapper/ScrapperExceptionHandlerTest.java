package backend.academy.linktracker.scrapper;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.configuration.SecurityConfig;
import backend.academy.linktracker.scrapper.infrastructure.api.ScrapperController;
import backend.academy.linktracker.scrapper.infrastructure.api.auth.JwtAuthenticationEntryPoint;
import backend.academy.linktracker.scrapper.infrastructure.api.errors.InvalidLinkException;
import backend.academy.linktracker.scrapper.infrastructure.api.mappers.SubscriptionToLinkResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.GetUsersSubscriptionsUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.SubscribeUserUseCase;
import backend.academy.linktracker.scrapper.infrastructure.api.usecases.UnsubscribeUserUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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
class ScrapperExceptionHandlerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

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
    void unsupportedLinkErrorKeepsFriendlyDetailsWithoutStacktrace() throws Exception {
        when(subscribeUseCase.execute(eq(USER_ID), any(), any()))
                .thenThrow(new InvalidLinkException("Сервис не поддерживается"));

        mockMvc.perform(post("/api/links")
                        .with(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://example.com/article\",\"tags\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"))
                .andExpect(jsonPath("$.exceptionName").value("InvalidLinkException"))
                .andExpect(jsonPath("$.exceptionMessage").value("Сервис не поддерживается"))
                .andExpect(jsonPath("$.stacktrace").doesNotExist());
    }

    @Test
    void internalErrorHidesInternals() throws Exception {
        when(subscribeUseCase.execute(eq(USER_ID), any(), any()))
                .thenThrow(new RuntimeException("jdbc:postgresql://db:5432/scrapper внутренняя деталь"));

        mockMvc.perform(post("/api/links")
                        .with(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\"https://github.com/user/repo\",\"tags\":[]}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.description").value("Внутренняя ошибка сервера"))
                .andExpect(jsonPath("$.exceptionName").value(nullValue()))
                .andExpect(jsonPath("$.exceptionMessage").value(nullValue()))
                .andExpect(jsonPath("$.stacktrace").doesNotExist());
    }

    @Test
    void malformedLinkReturnsBadRequestWithoutStacktrace() throws Exception {
        mockMvc.perform(post("/api/links")
                        .with(auth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"link\":\":::broken uri:::\",\"tags\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Некорректное тело запроса"))
                .andExpect(jsonPath("$.exceptionName").value(nullValue()))
                .andExpect(jsonPath("$.exceptionMessage").value(nullValue()))
                .andExpect(jsonPath("$.stacktrace").doesNotExist());
    }
}
