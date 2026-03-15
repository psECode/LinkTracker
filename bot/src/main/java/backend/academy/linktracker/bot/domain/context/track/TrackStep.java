package backend.academy.linktracker.bot.domain.context.track;

import backend.academy.linktracker.bot.domain.context.ContextStep;

public enum TrackStep implements ContextStep {
    AWAITING_LINK,
    AWAITING_TAGS,
    COMPLETED;

    @Override
    public ContextStep next() {
        int nextIndex = this.ordinal() + 1;
        return (nextIndex < values().length) ? values()[nextIndex] : COMPLETED;
    }

    @Override
    public boolean isTerminal() {
        return this == COMPLETED;
    }
}
