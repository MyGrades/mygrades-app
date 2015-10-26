package de.mygrades.main.events;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.database.dao.GradeEntry;

/**
 * Event to send grades to an activity.
 */
public class GradesEvent {
    private List<GradeEntry> grades;
    private boolean isScrapingResult;

    public GradesEvent() {
        grades = new ArrayList<>();
        isScrapingResult = false;
    }

    public GradesEvent(List<GradeEntry> grades) {
        this.grades = grades;
        isScrapingResult = false;
    }

    public GradesEvent(List<GradeEntry> grades, boolean isScrapingResult) {
        this.grades = grades;
        this.isScrapingResult = isScrapingResult;
    }

    public void setGrades(List<GradeEntry> grades) {
        this.grades = grades;
    }

    public List<GradeEntry> getGrades() {
        return grades;
    }

    public boolean isScrapingResult() {
        return isScrapingResult;
    }

    public void setIsScrapingResult(boolean isScrapingResult) {
        this.isScrapingResult = isScrapingResult;
    }
}
