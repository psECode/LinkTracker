package backend.academy.linktracker.bot.domain.context.track;

import backend.academy.linktracker.bot.domain.context.BotContext;
import java.util.List;
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
public class TrackContext implements BotContext {
    private Long chatId;
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
