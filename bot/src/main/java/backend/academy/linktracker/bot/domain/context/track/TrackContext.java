package backend.academy.linktracker.bot.domain.context.track;

import backend.academy.linktracker.bot.domain.context.BotContext;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TrackContext implements BotContext {
    private final Long chatId;
    private TrackStep step;

    private String link;
    private List<String> tags;

    @Override
    public void nextStep() {
        this.step = (TrackStep) this.step.next();
    }

    @Override
    public boolean isFinished() {
        return this.step.isTerminal();
    }
}
