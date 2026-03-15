package backend.academy.linktracker.bot.domain.context.track;

import java.util.Arrays;
import java.util.List;

public class TagsParsingUtil {
    public static List<String> parseTags(String tagsPart) {
        if (tagsPart == null || tagsPart.isBlank() || tagsPart.equalsIgnoreCase("нет")) {
            return List.of();
        }
        return Arrays.stream(tagsPart.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
