package backend.academy.linktracker.bot.domain.bot;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommandType {
    START("^/start$"),
    HELP("^/help$"),
    TRACK("^/track(\\s+.*)?$"),
    LIST("^/list(\\s+.*)?$"),
    UNTRACK("^/untrack(\\s+.*)?$"),
    UNKNOWN(null);

    private final String regex;

    public static CommandType fromText(String text) {
        if (text == null) return UNKNOWN;

        return Arrays.stream(values())
                .filter(type -> type.regex != null && text.trim().matches(type.regex))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
