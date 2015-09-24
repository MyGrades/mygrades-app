package de.mygrades.view.model;

/**
 * Semester item used in GradesRecyclerViewAdapter.
 */
public class SemesterItem implements GradesAdapterItem {
    private int termCount;
    private String termAsString;
    private float average;
    private float creditPoints;

    public SemesterItem() { }

    public SemesterItem(int termCount, String termAsString, float average, float creditPoints) {
        this.termCount = termCount;
        this.termAsString = termAsString;
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
