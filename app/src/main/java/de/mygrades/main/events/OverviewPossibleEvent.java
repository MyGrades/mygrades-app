package de.mygrades.main.events;

/**
 * Event to send information whether its possible to get an overview to subscribers, e.g. activities.
 */
public class OverviewPossibleEvent {
    private boolean isOverviewPossible;

    public OverviewPossibleEvent(boolean isOverviewPossible) {
        this.isOverviewPossible = isOverviewPossible;
    }

    public boolean isOverviewPossible() {
        return isOverviewPossible;
    }

    public void setIsOverviewPossible(boolean isOverviewPossible) {
        this.isOverviewPossible = isOverviewPossible;
    }
}
