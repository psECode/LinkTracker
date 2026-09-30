package backend.academy.linktracker.scrapper.infrastructure.api.auth;

import backend.academy.linktracker.scrapper.infrastructure.api.dtos.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class InternalTokenFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Internal-Token";
    private static final String INTERNAL_PREFIX = "/internal";

    private final ObjectMapper objectMapper;
    private final byte[] expectedToken;

    public InternalTokenFilter(ObjectMapper objectMapper, String internalToken) {
        this.objectMapper = objectMapper;
        this.expectedToken = internalToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith(INTERNAL_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        boolean valid =
                provided != null && MessageDigest.isEqual(expectedToken, provided.getBytes(StandardCharsets.UTF_8));

        if (!valid) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(
                    response.getWriter(),
                    new ApiErrorResponse(
                            "Не авторизован",
                            "401",
                            "Unauthorized",
                            "Отсутствует или неверен заголовок X-Internal-Token",
                            List.of()));
            return;
        }

        chain.doFilter(request, response);
    }
}
