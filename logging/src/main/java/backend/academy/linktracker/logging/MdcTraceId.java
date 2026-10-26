package backend.academy.linktracker.logging;

import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.MDC;

public final class MdcTraceId {

    public static final String TRACE_ID = "trace_id";

    private MdcTraceId() {}

    public static String newTraceId() {
        return UUID.randomUUID().toString();
    }

    public static String current() {
        return MDC.get(TRACE_ID);
    }

    public static void run(String traceId, Runnable action) {
        call(traceId, () -> {
            action.run();
            return null;
        });
    }

    public static <T> T call(String traceId, Supplier<T> action) {
        String previous = MDC.get(TRACE_ID);
        MDC.put(TRACE_ID, traceId);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                MDC.remove(TRACE_ID);
            } else {
                MDC.put(TRACE_ID, previous);
            }
        }
    }
}
