package de.mygrades.view.adapter.model;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.util.AverageCalculator;

/**
 * Semester item used in GradesRecyclerViewAdapter.
 */
public class SemesterItem implements GradesAdapterItem {
    private int semesterNumber;
    private String semester;
    private float average;
    private float creditPoints;
    private List<GradeItem> grades;

    public SemesterItem() {
        grades = new ArrayList<>();
    }

    public void addGrade(GradeItem gradeItem) {
        grades.add(gradeItem);
        update();
    }

    /**
     * Updates the average grade and the sum of credit points.
     */
    public void update() {
        AverageCalculator calculator = new AverageCalculator();
        calculator.calculate(grades);

        this.average = calculator.getAverage();
        this.creditPoints = calculator.getCreditPointsSum();
    }

    public int getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(int semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
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
