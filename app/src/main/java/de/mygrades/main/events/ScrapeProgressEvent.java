package de.mygrades.main.events;

/**
 * Created by tilman on 03.10.15.
 */
public class ScrapeProgressEvent {
    private int currentStep;
    private int stepCount;

    public ScrapeProgressEvent(int currentStep, int stepCount) {
        this.currentStep = currentStep;
        this.stepCount = stepCount;
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
}
