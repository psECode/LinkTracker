package backend.academy.linktracker.logging;

import java.util.LinkedHashMap;
import java.util.Map;

public final class LogArgs extends LinkedHashMap<String, Object> {

    private LogArgs(int initialCapacity) {
        super(initialCapacity);
    }

    public static LogArgs of(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("expected key-value pairs, got " + keyValuePairs.length + " values");
        }
        LogArgs result = new LogArgs(keyValuePairs.length / 2);
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            Object key = keyValuePairs[i];
            if (!(key instanceof String stringKey) || stringKey.isBlank()) {
                throw new IllegalArgumentException("arg name must be non-blank string, got: " + key);
            }
            result.put(stringKey, keyValuePairs[i + 1]);
        }
        return result;
    }

    public LogArgs and(Map<String, ?> more) {
        putAll(more);
        return this;
    }
}
