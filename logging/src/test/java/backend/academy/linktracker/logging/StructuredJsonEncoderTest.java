package backend.academy.linktracker.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class StructuredJsonEncoderTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final StructuredJsonEncoder encoder = new StructuredJsonEncoder();

    @BeforeEach
    void setUp() {
        MDC.clear();
        encoder.setServiceName("test-service");
        encoder.setEnv("test-env");
        encoder.setPodName("test-pod-0");
        encoder.start();
    }

    @Test
    void shouldEncodeRecordWithTemplateMessageAndArgs() throws Exception {
        LoggingEvent event = event(Level.INFO, "Message from {}: {}", new Object[] {12345L, "hello"});

        JsonNode record = mapper.readTree(encoder.encode(event));

        assertEquals("INFO", record.path("level").asText());
        assertEquals("Message from {}: {}", record.path("message").asText());
        assertEquals(12345L, record.path("args").get(0).asLong());
        assertEquals("hello", record.path("args").get(1).asText());
        assertEquals("test-service", record.path("service.name").asText());
        assertEquals("test-env", record.path("env").asText());
        assertEquals("test-pod-0", record.path("pod").asText());
        assertEquals(
                getClass().getPackageName() + ".StructuredJsonEncoderTest",
                record.path("logger").asText());
        assertEquals("2025-10-24T18:20:00.123Z", record.path("@timestamp").asText());
        String raw = new String(encoder.encode(event), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(raw.endsWith("\n"));
    }

    @Test
    void shouldMaskSecretsInsideArgs() throws Exception {
        LoggingEvent event = event(Level.INFO, "Requesting StackExchange API: {}", new Object[] {
            "https://api.stackexchange.com/2.3/search?site=stackoverflow&key=SECRET"
        });

        JsonNode record = mapper.readTree(encoder.encode(event));

        assertEquals(
                "https://api.stackexchange.com/2.3/search?site=stackoverflow&key=[REDACTED]",
                record.path("args").get(0).asText());
    }

    @Test
    void shouldAddStackTraceAndMdc() throws Exception {
        MDC.put("request_id", "req-42");
        try {
            LoggingEvent event = event(
                    Level.ERROR,
                    "Failed to fetch {}",
                    new Object[] {"https://github.com/user/repo"},
                    new IllegalStateException("boom"));

            JsonNode record = mapper.readTree(encoder.encode(event));

            assertEquals("req-42", record.path("request_id").asText());
            assertTrue(record.path("stack_trace").asText().contains("java.lang.IllegalStateException: boom"));
        } finally {
            MDC.clear();
        }
    }

    @Test
    void shouldOmitArgsFieldWhenThereAreNoArguments() throws Exception {
        LoggingEvent event = event(Level.WARN, "simple message", null);

        JsonNode record = mapper.readTree(encoder.encode(event));

        assertTrue(record.path("args").isMissingNode());
    }

    @Test
    void shouldFallBackToServiceNameAndEnvDefaults() throws Exception {
        StructuredJsonEncoder fromEnv = new StructuredJsonEncoder();
        fromEnv.start();

        LoggingEvent event = event(Level.INFO, "ping", null);
        JsonNode record = mapper.readTree(fromEnv.encode(event));

        assertEquals("unknown", record.path("service.name").asText());
        assertTrue(record.hasNonNull("env"));
    }

    private LoggingEvent event(Level level, String message, Object[] args) {
        return event(level, message, args, null);
    }

    private LoggingEvent event(Level level, String message, Object[] args, Throwable throwable) {
        LoggingEvent event =
                new LoggingEvent(StructuredJsonEncoder.class.getName(), LOGGER, level, message, throwable, args);
        event.setInstant(Instant.ofEpochMilli(1761330000123L));
        return event;
    }

    private static final Logger LOGGER = (Logger) org.slf4j.LoggerFactory.getLogger(StructuredJsonEncoderTest.class);
}
