package dh.mygrades.view.adapter.model;

import java.util.ArrayList;
import java.util.List;

import dh.mygrades.util.AverageCalculator;

/**
 * Semester item used in GradesRecyclerViewAdapter.
 */
public class SemesterItem implements GradesAdapterItem {
    private String semester;
    private float average;
    private float creditPoints;
    private List<GradeItem> grades;
    private boolean simpleWeighting;

    public SemesterItem(boolean simpleWeighting) {
        grades = new ArrayList<>();
        this.simpleWeighting = simpleWeighting;
    }

    public void addGrade(GradeItem gradeItem, boolean updateImmediately) {
        grades.add(gradeItem);

        if (updateImmediately) {
            update();
        }
    }

    /**
     * Updates the average grade and the sum of credit points.
     */
    public void update() {
        AverageCalculator calculator = new AverageCalculator(simpleWeighting);
        calculator.calculate(grades);

        this.average = calculator.getAverage();
        this.creditPoints = calculator.getCreditPointsSum();
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

    public List<GradeItem> getGrades() {
        return grades;
    }

    public int getVisibleGradesCount() {
        int count = 0;
        for (GradeItem gradeItem : grades) {
            count += gradeItem.isHidden() ? 0 : 1;
        }
        return count;
    }

    public void setSimpleWeighting(boolean simpleWeighting) {
        this.simpleWeighting = simpleWeighting;
        update();
    }
}
