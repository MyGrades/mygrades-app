package de.mygrades.main.events;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.view.adapter.model.SemesterItem;

/**
 * Event to post statistics to subscribers.
 */
public class StatisticsEvent {
    private float average;
    private float creditPoints;
    private float creditPointsPerSemester;
    private float studyProgress;
    private int gradeCount;
    private List<SemesterItem> semesterItems;

    // sections for grade distribution [1.0 -1.3, 1.7-2.3, 2.7-3.3, 3.7 - 4.0, 4.3 - 5.0, others]
    private int[] gradeDistribution;

    public StatisticsEvent() {
        semesterItems = new ArrayList<>();
        gradeDistribution = new int[6];
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

    public float getCreditPointsPerSemester() {
        return creditPointsPerSemester;
    }

    public void setCreditPointsPerSemester(float creditPointsPerSemester) {
        this.creditPointsPerSemester = creditPointsPerSemester;
    }

    public float getStudyProgress() {
        return studyProgress;
    }

    public void setStudyProgress(float studyProgress) {
        this.studyProgress = studyProgress;
    }

    public int getGradeCount() {
        return gradeCount;
    }

    public void setGradeCount(int gradeCount) {
        this.gradeCount = gradeCount;
    }

    public List<SemesterItem> getSemesterItems() {
        return semesterItems;
    }

    public void setSemesterItems(List<SemesterItem> semesterItems) {
        this.semesterItems = semesterItems;
    }

    public int[] getGradeDistribution() {
        return gradeDistribution;
    }

    public void setGradeDistribution(int[] gradeDistribution) {
        this.gradeDistribution = gradeDistribution;
    }
}
