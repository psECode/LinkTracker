package backend.academy.linktracker.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class MdcTraceIdTest {

    @AfterEach
    void tearDown() {
        MDC.remove(MdcTraceId.TRACE_ID);
    }

    @Test
    void shouldPutTraceIdDuringRun() {
        MdcTraceId.run("abc", () -> assertEquals("abc", MdcTraceId.current()));
        assertNull(MdcTraceId.current());
    }

    @Test
    void shouldRestorePreviousTraceIdAfterNestedRun() {
        MdcTraceId.run("outer", () -> {
            MdcTraceId.run("inner", () -> assertEquals("inner", MdcTraceId.current()));
            assertEquals("outer", MdcTraceId.current());
        });
        assertNull(MdcTraceId.current());
    }

    @Test
    void shouldReturnCallResult() {
        String result = MdcTraceId.call("abc", () -> "value");
        assertEquals("value", result);
        assertNull(MdcTraceId.current());
    }

    @Test
    void shouldRestoreTraceIdAfterException() {
        try {
            MdcTraceId.run("abc", () -> {
                throw new IllegalStateException("boom");
            });
        } catch (IllegalStateException expected) {
            // ок
        }
        assertNull(MdcTraceId.current());
    }

    @Test
    void shouldGenerateUniqueTraceIds() {
        MDC.put(MdcTraceId.TRACE_ID, "kept");
        assertEquals("kept", MdcTraceId.current());

        String first = MdcTraceId.newTraceId();
        String second = MdcTraceId.newTraceId();
        assertEquals(first, first);
        assertEquals(36, first.length());
        assertEquals(36, second.length());
    }
}
