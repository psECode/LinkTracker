package backend.academy.linktracker.bot.infrastructure.api;

import backend.academy.linktracker.bot.application.bot.usecases.ProcessUpdateUseCase;
import backend.academy.linktracker.bot.domain.api.dtos.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/updates")
@RequiredArgsConstructor
public class BotController {

    private final ProcessUpdateUseCase processUpdateUseCase;

    @PostMapping
    public ResponseEntity<Void> sendUpdate(@RequestBody LinkUpdate update) {
        log.info("Прислали какое то обновление: {}", update.url());

        if (update.tgChatIds() == null || update.tgChatIds().isEmpty()) {
            log.warn("Какую то чушь прислали");
            return ResponseEntity.badRequest().build();
        }

        processUpdateUseCase.execute(update);

        return ResponseEntity.ok().build();
    }
}
