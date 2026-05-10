package backend.academy.linktracker.bot.domain.context.untrack;

import backend.academy.linktracker.bot.domain.context.BotContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UntrackContext implements BotContext {
    private final Long chatId;
    private UntrackStep step;

    private String link;

    @Override
    public void nextStep() {
        this.step = (UntrackStep) this.step.next();
    }

    @Override
    public boolean isFinished() {
        return this.step.isTerminal();
    }
}
