package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.application.notifications.usecases.MarkNotificationsReadUseCase;
import backend.academy.linktracker.scrapper.application.notifications.usecases.ReadNotificationsUseCase;
import backend.academy.linktracker.scrapper.domain.notifications.dtos.NotificationsPage;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.MarkReadRequest;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.MarkReadResponse;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.NotificationDto;
import backend.academy.linktracker.scrapper.infrastructure.api.dtos.NotificationsResponse;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final ReadNotificationsUseCase readNotificationsUseCase;
    private final MarkNotificationsReadUseCase markNotificationsReadUseCase;
    private final MeterRegistry meterRegistry;

    @GetMapping
    public ResponseEntity<NotificationsResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "unread", required = false, defaultValue = "false") boolean unread,
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limit) {
        UUID userId = UUID.fromString(jwt.getSubject());
        meterRegistry.counter("api_requests_total", "source", "web").increment();

        NotificationsPage page = readNotificationsUseCase.execute(userId, unread, limit);
        List<NotificationDto> items = page.items().stream()
                .map(n -> new NotificationDto(n.id(), n.linkId(), n.message(), n.createdAt(), n.readAt()))
                .toList();

        return ResponseEntity.ok(new NotificationsResponse(items, page.unreadCount()));
    }

    @PostMapping("/read")
    public ResponseEntity<MarkReadResponse> markRead(
            @AuthenticationPrincipal Jwt jwt, @RequestBody MarkReadRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        meterRegistry.counter("api_requests_total", "source", "web").increment();

        boolean markAll = Boolean.TRUE.equals(request.all())
                || request.ids() == null
                || request.ids().isEmpty();
        int updated = markNotificationsReadUseCase.execute(userId, request.ids(), markAll);

        return ResponseEntity.ok(new MarkReadResponse(updated));
    }
}
