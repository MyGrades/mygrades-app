package de.mygrades.view.adapter.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Semester item used in GradesRecyclerViewAdapter.
 */
public class SemesterItem implements GradesAdapterItem {
    private int termCount;
    private String termAsString;
    private float average;
    private float creditPoints;
    private List<GradeItem> grades;

    public SemesterItem() {
        grades = new ArrayList<>();
    }

    public SemesterItem(int termCount, String termAsString, float average, float creditPoints) {
        this.termCount = termCount;
        this.termAsString = termAsString;
        this.average = average;
        this.creditPoints = creditPoints;
        grades = new ArrayList<>();
    }

    public void addGrade(GradeItem gradeItem) {
        grades.add(gradeItem);

        // update creditPoints and average
        // TODO: consider credit points for average calculation!
        float average = 0f;
        float creditPoints = 0f;
        for(GradeItem grade : grades) {
            average += grade.getGrade();
            creditPoints += grade.getCreditPoints();
        }
        average /= grades.size();

        this.average = average;
        this.creditPoints = creditPoints;
    }

    public int getTermCount() {
        return termCount;
    }

    public void setTermCount(int termCount) {
        this.termCount = termCount;
    }

    public String getTermAsString() {
        return termAsString;
    }

    public void setTermAsString(String termAsString) {
        this.termAsString = termAsString;
    }

    public float getAverage() {
        return average;
    }

    public void setAverage(float average) {
        this.average = average;
    }

    public float getCreditPoints() {
        return creditPoints;
    }

    public void setCreditPoints(float creditPoints) {
        this.creditPoints = creditPoints;
    }
}
