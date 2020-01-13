package dh.mygrades.main.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dh.mygrades.database.dao.GradeEntry;

/**
 * Event to send grades to an activity.
 */
public class GradesEvent {
    private List<GradeEntry> grades;
    private boolean isScrapingResult;
    private Map<String, Integer> semesterToSemesterNumberMap;
    private String actualFirstSemester;

    public GradesEvent(List<GradeEntry> grades, Map<String, Integer> semesterToSemesterNumberMap, String actualFirstSemester) {
        this.grades = grades;
        this.semesterToSemesterNumberMap = semesterToSemesterNumberMap;
        this.actualFirstSemester = actualFirstSemester;
        isScrapingResult = false;
    }

    public GradesEvent(List<GradeEntry> grades, boolean isScrapingResult, Map<String, Integer> semesterToSemesterNumberMap, String actualFirstSemester) {
        this.grades = grades;
        this.isScrapingResult = isScrapingResult;
        this.semesterToSemesterNumberMap = semesterToSemesterNumberMap;
        this.actualFirstSemester = actualFirstSemester;
    }

    public GradesEvent(GradeEntry gradeEntry) {
        grades = new ArrayList<>();
        grades.add(gradeEntry);
        isScrapingResult = false;
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

    public Map<String, Integer> getSemesterToSemesterNumberMap() {
        return semesterToSemesterNumberMap;
    }

    public String getActualFirstSemester() {
        return actualFirstSemester;
    }
}
