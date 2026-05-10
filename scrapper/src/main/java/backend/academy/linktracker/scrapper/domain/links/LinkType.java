package backend.academy.linktracker.scrapper.domain.links;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LinkType {
    GITHUB(Pattern.compile("^https?://(?:www\\.)?github\\.com/([^/]+)/([^/]+)/?$")),

    STACKOVERFLOW(Pattern.compile("^https?://(?:www\\.)?stackoverflow\\.com/questions/(\\d+)(?:/.*)?$"));

    private final Pattern pattern;

    public static Optional<LinkType> of(String url) {
        if (url == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(type -> type.pattern.matcher(url).matches())
                .findFirst();
    }
}
