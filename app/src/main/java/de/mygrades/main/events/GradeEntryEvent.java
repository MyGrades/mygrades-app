package de.mygrades.main.events;

import java.util.Map;

import de.mygrades.database.dao.GradeEntry;

/**
 * Event to send a GradeEntry to subscribers, e.g. activities.
 */
public class GradeEntryEvent {
    private GradeEntry gradeEntry;
    private Map<String, Integer> semesterToSemesterNumberMap;

    public GradeEntryEvent(GradeEntry gradeEntry) {
        this.gradeEntry = gradeEntry;
    }

    public GradeEntry getGradeEntry() {
        return gradeEntry;
    }

    public void setGradeEntry(GradeEntry gradeEntry) {
        this.gradeEntry = gradeEntry;
    }

    public Map<String, Integer> getSemesterToSemesterNumberMap() {
        return semesterToSemesterNumberMap;
    }

    public void setSemesterToSemesterNumberMap(Map<String, Integer> semesterToSemesterNumberMap) {
        this.semesterToSemesterNumberMap = semesterToSemesterNumberMap;
    }
}
