package backend.academy.linktracker.bot.domain.context.untrack;

import backend.academy.linktracker.bot.domain.context.ContextStep;

public enum UntrackStep implements ContextStep {
    WAITING_FOR_LINK,
    COMPLETED;

    @Override
    public ContextStep next() {
        return COMPLETED;
    }

    @Override
    public boolean isTerminal() {
        return this == COMPLETED;
    }
}
