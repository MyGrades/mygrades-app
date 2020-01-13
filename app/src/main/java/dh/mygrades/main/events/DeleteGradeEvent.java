package dh.mygrades.main.events;

import dh.mygrades.database.dao.GradeEntry;

/**
 * Event to send a grade which should be deleted to an activity.
 */
public class DeleteGradeEvent {
    private GradeEntry gradeEntry;

    public DeleteGradeEvent(GradeEntry gradeEntry) {
        this.gradeEntry = gradeEntry;
    }

    public GradeEntry getGradeEntry() {
        return gradeEntry;
    }
}
