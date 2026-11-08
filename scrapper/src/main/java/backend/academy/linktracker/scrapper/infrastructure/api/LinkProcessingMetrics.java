package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.infrastructure.api.errors.ApiClientException;
import io.micrometer.core.instrument.MeterRegistry;

public final class LinkProcessingMetrics {

    public static final String CLIENT_ERROR_TYPE = "client";
    public static final String SERVER_ERROR_TYPE = "server";

    private LinkProcessingMetrics() {
    }

    public static void recordSuccess(MeterRegistry registry, String operation) {
        registry.counter(
                        "links_tracked_total",
                        "operation", operation,
                        "outcome", "success")
                .increment();
    }

    public static void recordFailure(MeterRegistry registry, String operation, RuntimeException error) {
        registry.counter(
                        "links_tracked_total",
                        "operation", operation,
                        "outcome", "failure",
                        "error_type", resolveErrorType(error))
                .increment();
    }

    private static String resolveErrorType(RuntimeException error) {
        return error instanceof ApiClientException ? CLIENT_ERROR_TYPE : SERVER_ERROR_TYPE;
    }
}
