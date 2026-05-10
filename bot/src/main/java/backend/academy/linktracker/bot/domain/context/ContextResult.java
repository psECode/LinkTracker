package backend.academy.linktracker.bot.domain.context;

public record ContextResult(boolean handled, String message, boolean isFinished) {
    public static ContextResult success(String msg, boolean finished) {
        return new ContextResult(true, msg, finished);
    }

    public static ContextResult reject() {
        return new ContextResult(false, null, false);
    }
}
