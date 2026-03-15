package backend.academy.linktracker.bot.domain.context;

public interface ContextStep {
    ContextStep next();

    boolean isTerminal();
}
