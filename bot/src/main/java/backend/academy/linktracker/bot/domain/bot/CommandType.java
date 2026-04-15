package backend.academy.linktracker.bot.domain.bot;

import java.util.Arrays;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public enum CommandType {
    START("^/start$"),
    HELP("^/help$"),
    TRACK("^/track(\\s+.*)?$"),
    LIST("^/list(\\s+.*)?$"),
    UNTRACK("^/untrack(\\s+.*)?$"),
    UNKNOWN(null);

    private final Pattern pattern;

    CommandType(String regex) {
        if (regex != null) {
            this.pattern = Pattern.compile(regex);
        } else {
            this.pattern = null;
        }
    }

    public static CommandType fromText(String text) {
        if (text == null) return UNKNOWN;

        String trimmedText = text.trim();

        return Arrays.stream(values())
                .filter(type -> type.pattern != null
                        && type.pattern.matcher(trimmedText).matches())
                .findFirst()
                .orElse(UNKNOWN);
    }
}
