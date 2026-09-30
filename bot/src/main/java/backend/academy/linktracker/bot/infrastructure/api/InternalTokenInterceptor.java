package backend.academy.linktracker.bot.infrastructure.api;

import backend.academy.linktracker.bot.infrastructure.api.dtos.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.web.servlet.HandlerInterceptor;

public class InternalTokenInterceptor implements HandlerInterceptor {

    private static final String HEADER = "X-Internal-Token";

    private final ObjectMapper objectMapper;
    private final byte[] expectedToken;

    public InternalTokenInterceptor(ObjectMapper objectMapper, String internalToken) {
        this.objectMapper = objectMapper;
        this.expectedToken = internalToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String provided = request.getHeader(HEADER);
        boolean valid =
                provided != null && MessageDigest.isEqual(expectedToken, provided.getBytes(StandardCharsets.UTF_8));

        if (!valid) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            objectMapper.writeValue(
                    response.getWriter(),
                    new ApiErrorResponse(
                            "Не авторизован",
                            "401",
                            "Unauthorized",
                            "Отсутствует или неверен заголовок X-Internal-Token",
                            List.of()));
            return false;
        }
        return true;
    }
}
