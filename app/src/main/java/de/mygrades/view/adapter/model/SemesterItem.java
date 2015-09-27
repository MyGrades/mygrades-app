package de.mygrades.view.adapter.model;

import java.util.ArrayList;
import java.util.List;

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

    public SemesterItem(int semesterNumber, String semester, float average, float creditPoints) {
        this.semesterNumber = semesterNumber;
        this.semester = semester;
        this.average = average;
        this.creditPoints = creditPoints;
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
        // update creditPoints and average
        float average = 0f;
        float creditPointsSum = 0f;
        float creditPointsSumForAverage = 0f; // sum grade_entries may have credit points, but no grade

        for(GradeItem grade : grades) {
            float actCreditPoints = (grade.getCreditPoints() == null ? 0f : grade.getCreditPoints());
            creditPointsSum += actCreditPoints;

            if (grade.getGrade() != null && actCreditPoints > 0) {
                creditPointsSumForAverage += actCreditPoints;
            }
            average += (grade.getGrade() == null ? 0f : grade.getGrade() * actCreditPoints);

        }

        average = creditPointsSumForAverage > 0 ? average/creditPointsSumForAverage : 0f;

        this.average = average;
        this.creditPoints = creditPointsSum;
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
