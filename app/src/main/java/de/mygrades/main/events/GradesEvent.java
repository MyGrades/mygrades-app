package de.mygrades.main.events;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.database.dao.GradeEntry;

/**
 * Event to send grades to an activity.
 */
public class GradesEvent {
    private List<GradeEntry> grades;

    public GradesEvent() {
        grades = new ArrayList<>();
    }

    public GradesEvent(List<GradeEntry> grades) {
        this.grades = grades;
    }

    public void setGrades(List<GradeEntry> grades) {
        this.grades = grades;
    }

    public List<GradeEntry> getGrades() {
        return grades;
    }
}
