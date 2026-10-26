package backend.academy.linktracker.logging;

import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public final class SensitiveDataMasker {

    public static final String REDACTED = "[REDACTED]";

    private static final Pattern TELEGRAM_BOT_TOKEN = Pattern.compile("\\b\\d{8,10}:[A-Za-z0-9_-]{30,}\\b");
    private static final Pattern JWT =
            Pattern.compile("\\beyJ[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\.[A-Za-z0-9_-]{10,}\\b");
    private static final Pattern GITHUB_TOKEN = Pattern.compile("\\bgh[pousr]_[A-Za-z0-9]{20,}\\b");
    private static final Pattern BEARER_TOKEN = Pattern.compile("(?i)\\bbearer\\s+[A-Za-z0-9._~+/=-]{8,}");
    private static final Pattern URL_SECRET_PARAM = Pattern.compile(
            "(?i)([?&](?:access_token|api_?key|client_secret|token|secret|password|passwd|pass|key)=)[^&\\s\"']+");
    private static final Pattern PUSH_ENDPOINT_TOKEN =
            Pattern.compile("(?i)(fcm\\.googleapis\\.com/fcm/send/)[A-Za-z0-9_-]{8,}");
    private static final Pattern EMAIL = Pattern.compile("([A-Za-z0-9._%+-]+)@([A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+)");

    private static final List<MaskRule> RULES = List.of(
            new MaskRule(TELEGRAM_BOT_TOKEN, matchResult -> REDACTED),
            new MaskRule(JWT, matchResult -> REDACTED),
            new MaskRule(GITHUB_TOKEN, matchResult -> REDACTED),
            new MaskRule(BEARER_TOKEN, matchResult -> "Bearer " + REDACTED),
            new MaskRule(URL_SECRET_PARAM, matchResult -> matchResult.group(1) + REDACTED),
            new MaskRule(PUSH_ENDPOINT_TOKEN, matchResult -> matchResult.group(1) + REDACTED),
            new MaskRule(EMAIL, matchResult -> maskEmail(matchResult.group(1), matchResult.group(2))));

    public String mask(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String result = text;
        for (MaskRule rule : RULES) {
            result = rule.apply(result);
        }
        return result;
    }

    private static String maskEmail(String localPart, String domain) {
        if (localPart.length() <= 1) {
            return "*@" + domain;
        }
        int hidden = Math.min(localPart.length() - 1, 10);
        return localPart.charAt(0) + "*".repeat(hidden) + "@" + domain;
    }

    private record MaskRule(Pattern pattern, Function<MatchResult, String> replacement) {
        String apply(String text) {
            return pattern.matcher(text).replaceAll(matchResult -> replacement.apply(matchResult));
        }
    }
}
