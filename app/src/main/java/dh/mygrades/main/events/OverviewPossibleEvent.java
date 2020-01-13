package dh.mygrades.main.events;

/**
 * Event to send information whether its possible to get an overview to subscribers, e.g. activities.
 */
public class OverviewPossibleEvent {
    private boolean isOverviewPossible;
    private boolean isOverviewPossibleForUniversity;

    public OverviewPossibleEvent(boolean isOverviewPossible, boolean isOverviewPossibleForUniversity) {
        this.isOverviewPossible = isOverviewPossible;
        this.isOverviewPossibleForUniversity = isOverviewPossibleForUniversity;
    }

    public boolean isOverviewPossible() {
        return isOverviewPossible;
    }

    public void setIsOverviewPossible(boolean isOverviewPossible) {
        this.isOverviewPossible = isOverviewPossible;
    }

    public boolean isOverviewPossibleForUniversity() {
        return isOverviewPossibleForUniversity;
    }

    public void setIsOverviewPossibleForUniversity(boolean isOverviewPossibleForUniversity) {
        this.isOverviewPossibleForUniversity = isOverviewPossibleForUniversity;
    }
}
