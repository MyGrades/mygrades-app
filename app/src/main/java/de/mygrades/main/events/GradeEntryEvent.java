package de.mygrades.main.events;

import de.mygrades.database.dao.GradeEntry;

/**
 * Event to send a GradeEntry to subscribers, e.g. activities.
 */
public class GradeEntryEvent {
    private GradeEntry gradeEntry;

    public GradeEntryEvent(GradeEntry gradeEntry) {
        this.gradeEntry = gradeEntry;
    }

    public GradeEntry getGradeEntry() {
        return gradeEntry;
    }

    public void setGradeEntry(GradeEntry gradeEntry) {
        this.gradeEntry = gradeEntry;
    }
}
