package de.mygrades.main.events;

import de.mygrades.database.dao.Overview;

/**
 * Event to send a Overview to subscribers, e.g. activities.
 */
public class OverviewEvent {
    private Overview overview;
    private boolean isScrapingResult;

    public OverviewEvent(Overview overview) {
        this.overview = overview;
    }

    public OverviewEvent(Overview overview, boolean isScrapingResult) {
        this(overview);
        this.isScrapingResult = isScrapingResult;
    }

    public Overview getOverview() {
        return overview;
    }

    public void setOverview(Overview overview) {
        this.overview = overview;
    }

    public boolean isScrapingResult() {
        return isScrapingResult;
    }

    public void setIsScrapingResult(boolean isScrapingResult) {
        this.isScrapingResult = isScrapingResult;
    }
}
