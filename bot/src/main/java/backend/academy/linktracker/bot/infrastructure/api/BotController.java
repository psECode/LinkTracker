package backend.academy.linktracker.bot.infrastructure.api;

import backend.academy.linktracker.bot.application.bot.usecases.ProcessUpdateUseCase;
import backend.academy.linktracker.bot.domain.api.dtos.LinkUpdate;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
@RequiredArgsConstructor
public class BotController {

    private final ProcessUpdateUseCase processUpdateUseCase;

    @PostMapping
    @RateLimiter(name = "botApiLimit")
    public ResponseEntity<Void> sendUpdate(@Valid @RequestBody LinkUpdate update) {
        processUpdateUseCase.execute(update);
        return ResponseEntity.ok().build();
    }
}
