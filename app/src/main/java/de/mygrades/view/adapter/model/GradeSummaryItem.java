package de.mygrades.view.adapter.model;

/**
 * Created by tilman on 27.09.15.
 */
public class GradeSummaryItem implements GradesAdapterItem {
    private float average;
    private float creditPoints;
    private String lastUpdatedAt;

    public GradeSummaryItem() {
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

    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(String lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
