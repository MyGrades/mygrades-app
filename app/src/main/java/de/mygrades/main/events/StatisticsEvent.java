package de.mygrades.main.events;

/**
 * Event to post statistics to subscribers.
 */
public class StatisticsEvent {
    private float average;
    private float creditPoints;
    private float creditPointsPerSemester;
    private float studyProgress;
    private int gradeCount;

    public StatisticsEvent() {}

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
}
