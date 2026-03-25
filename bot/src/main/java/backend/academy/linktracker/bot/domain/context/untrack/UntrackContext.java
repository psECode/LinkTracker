package backend.academy.linktracker.bot.domain.context.untrack;

import backend.academy.linktracker.bot.domain.context.BotContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UntrackContext implements BotContext {
    private Long chatId;
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
