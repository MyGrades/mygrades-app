package de.mygrades.main.events;

/**
 * Created by tilman on 03.10.15.
 */
public class ScrapeProgressEvent {
    private int currentStep;
    private int stepCount;
    private boolean isScrapeForOverview;
    private String gradeHash;

    public ScrapeProgressEvent(int currentStep, int stepCount) {
        this.currentStep = currentStep;
        this.stepCount = stepCount;
        this.isScrapeForOverview = false;
        this.gradeHash = null;
    }

    public ScrapeProgressEvent(int currentStep, int stepCount, boolean isScrapeForOverview) {
        this(currentStep, stepCount);
        this.isScrapeForOverview = isScrapeForOverview;
    }

    public ScrapeProgressEvent(int currentStep, int stepCount, boolean isScrapeForOverview, String gradeHash) {
        this(currentStep, stepCount, isScrapeForOverview);
        this.gradeHash = gradeHash;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public boolean isScrapeForOverview() {
        return isScrapeForOverview;
    }

    public void setIsScrapeForOverview(boolean isScrapeForOverview) {
        this.isScrapeForOverview = isScrapeForOverview;
    }

    public String getGradeHash() {
        return gradeHash;
    }

    public void setGradeHash(String gradeHash) {
        this.gradeHash = gradeHash;
    }
}
