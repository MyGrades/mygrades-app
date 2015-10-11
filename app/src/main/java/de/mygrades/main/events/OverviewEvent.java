package de.mygrades.main.events;

import de.mygrades.database.dao.Overview;

/**
 * Event to send a Overview to subscribers, e.g. activities.
 */
public class OverviewEvent {
    private Overview overview;

    public OverviewEvent(Overview overview) {
        this.overview = overview;
    }

    public Overview getOverview() {
        return overview;
    }

    public void setOverview(Overview overview) {
        this.overview = overview;
    }
}
