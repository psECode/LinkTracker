package backend.academy.linktracker.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.encoder.EncoderBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class StructuredJsonEncoder extends EncoderBase<ILoggingEvent> {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final byte[] NEW_LINE = "\n".getBytes(StandardCharsets.UTF_8);
    private static final byte[] FALLBACK_RECORD =
            "{\"level\":\"ERROR\",\"message\":\"log record encoding failed\"}\n".getBytes(StandardCharsets.UTF_8);

    private final SensitiveDataMasker masker = new SensitiveDataMasker();

    private String serviceName = "unknown";
    private String serviceNameOverride;
    private String env;
    private String podName;

    public void setServiceName(String serviceName) {
        this.serviceNameOverride = serviceName;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    @Override
    public void start() {
        this.serviceName = resolveConfigured(serviceNameOverride, null, "unknown");
        this.env = resolveConfigured(env, "APP_ENV", "local");
        this.podName = resolveConfigured(podName, "POD_NAME", resolveConfigured(null, "HOSTNAME", "unknown"));
        super.start();
    }

    @Override
    public byte[] headerBytes() {
        return null;
    }

    @Override
    public byte[] footerBytes() {
        return null;
    }

    @Override
    public byte[] encode(ILoggingEvent event) {
        try {
            ObjectNode record = MAPPER.createObjectNode();
            record.put("@timestamp", TIMESTAMP_FORMAT.format(event.getInstant()));
            record.put("level", event.getLevel().toString());
            record.put("message", masker.mask(messageOf(event)));

            ArrayNode args = putArgs(record, event.getArgumentArray());
            if (args != null && !args.isEmpty()) {
                record.set("args", args);
            }

            record.put("logger", event.getLoggerName());
            record.put("thread", event.getThreadName());
            record.put("service.name", serviceName);
            record.put("env", env);
            record.put("pod", podName);

            Map<String, String> mdc;
            try {
                mdc = event.getMDCPropertyMap();
            } catch (Exception mdcFailure) {
                addError("failed to read MDC properties", mdcFailure);
                mdc = Map.of();
            }
            if (mdc != null) {
                mdc.forEach((key, value) -> record.put(key, masker.mask(value)));
            }

            IThrowableProxy throwable = event.getThrowableProxy();
            if (throwable != null) {
                record.put("stack_trace", masker.mask(ThrowableProxyUtil.asString(throwable)));
            }

            return concat(MAPPER.writeValueAsBytes(record), NEW_LINE);
        } catch (Exception encodingFailure) {
            addError("log record encoding failed", encodingFailure);
            return FALLBACK_RECORD;
        }
    }

    private static String messageOf(ILoggingEvent event) {
        String template = event.getMessage();
        return template != null ? template : event.getFormattedMessage();
    }

    private ArrayNode putArgs(ObjectNode record, Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return null;
        }
        ArrayNode args = record.putArray("args");
        for (Object argument : arguments) {
            if (argument == null) {
                args.addNull();
            } else if (argument instanceof String text) {
                args.add(masker.mask(text));
            } else {
                args.add(asJson(argument));
            }
        }
        return args;
    }

    private JsonNode asJson(Object argument) {
        try {
            return MAPPER.valueToTree(argument);
        } catch (Exception serializationFailure) {
            return MAPPER.getNodeFactory().textNode(masker.mask(String.valueOf(argument)));
        }
    }

    private static String resolveConfigured(String configured, String envVariable, String fallback) {
        if (configured != null && !configured.isBlank() && !configured.startsWith("${")) {
            return configured;
        }
        if (envVariable != null) {
            String fromEnv = System.getenv(envVariable);
            if (fromEnv != null && !fromEnv.isBlank()) {
                return fromEnv;
            }
        }
        return fallback;
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
