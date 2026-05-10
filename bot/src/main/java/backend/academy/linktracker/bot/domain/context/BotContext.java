package backend.academy.linktracker.bot.domain.context;

public interface BotContext {
    Long getChatId();

    ContextStep getStep();

    void nextStep();

    boolean isFinished();
}
