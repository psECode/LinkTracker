package backend.academy.linktracker.scrapper.infrastructure.api;

import backend.academy.linktracker.scrapper.domain.api.stackoverflow.StackoverflowClient;
import backend.academy.linktracker.scrapper.domain.links.LinkType;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StackoverflowLinkChecker implements LinkChecker {
    private final StackoverflowClient stackoverflowClient;

    public OffsetDateTime getLastUpdatedDate(String url) {
        String[] parts =
                url.replaceFirst("https?://stackoverflow\\.com/questions/", "").split("/");

        var response = stackoverflowClient.getQuestion(parts[0]);

        if (response.items() == null || response.items().isEmpty()) {
            log.warn("Вопрос с ID {} не найден на StackOverflow", parts[0]);
            return null;
        }

        return response.items().getFirst().lastActivityAsOffsetDateTime();
    }

    @Override
    public LinkType getType() {
        return LinkType.STACKOVERFLOW;
    }
}
