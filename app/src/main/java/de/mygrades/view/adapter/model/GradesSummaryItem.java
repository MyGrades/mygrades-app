package de.mygrades.view.adapter.model;

/**
 * Created by tilman on 27.09.15.
 */
public class GradesSummaryItem implements GradesAdapterItem {
    private float average;
    private float creditPoints;
    private String lastUpdatedAt;
    private String infoBoxTitle;
    private String infoBoxMessage;
    private boolean infoBoxVisible;
    private String dismissPrefKey;
    private boolean noGradesInfoVisible;

    public GradesSummaryItem() {
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

    public String getInfoBoxTitle() {
        return infoBoxTitle;
    }

    public void setInfoBoxTitle(String infoBoxTitle) {
        this.infoBoxTitle = infoBoxTitle;
    }

    public String getInfoBoxMessage() {
        return infoBoxMessage;
    }

    public void setInfoBoxMessage(String infoBoxMessage) {
        this.infoBoxMessage = infoBoxMessage;
    }

    public boolean isInfoBoxVisible() {
        return infoBoxVisible;
    }

    public void setInfoBoxVisible(boolean infoBoxVisible) {
        this.infoBoxVisible = infoBoxVisible;
    }

    public String getDismissPrefKey() {
        return dismissPrefKey;
    }

    public void setDismissPrefKey(String dismissPrefKey) {
        this.dismissPrefKey = dismissPrefKey;
    }

    public boolean isNoGradesInfoVisible() {
        return noGradesInfoVisible;
    }

    public void setNoGradesInfoVisible(boolean noGradesInfoVisible) {
        this.noGradesInfoVisible = noGradesInfoVisible;
    }
}
