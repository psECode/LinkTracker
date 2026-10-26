package backend.academy.linktracker.bot.infrastructure.http;

import backend.academy.linktracker.logging.MdcTraceId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


@Component
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String traceId = Optional.ofNullable(request.getHeader(TRACE_ID_HEADER))
                .filter(header -> !header.isBlank())
                .orElseGet(MdcTraceId::newTraceId);

        MDC.put(MdcTraceId.TRACE_ID, traceId);
        try {
            response.setHeader(TRACE_ID_HEADER, traceId);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MdcTraceId.TRACE_ID);
        }
    }
}
