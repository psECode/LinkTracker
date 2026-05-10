package backend.academy.linktracker.bot.application.context;

import backend.academy.linktracker.bot.domain.api.dtos.LinkResponse;
import java.util.List;

public class LinkListFormattingUtil {
    private LinkListFormattingUtil() {}

    public static String execute(List<LinkResponse> links) {
        StringBuilder sb = new StringBuilder("Ваши подписки:\n\n");
        for (var link : links) {
            sb.append(" ").append(link.url()).append("\n");
            if (!link.tags().isEmpty()) {
                sb.append("Теги: ").append(String.join(", ", link.tags())).append("\n\n");
            }
        }
        return sb.toString();
    }
}
