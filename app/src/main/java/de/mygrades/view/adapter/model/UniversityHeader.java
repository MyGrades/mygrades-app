package de.mygrades.view.adapter.model;

import de.mygrades.main.events.ErrorEvent;

/**
 * Header above list of sectioned universities.
 * It holds the state for the loading animation and current error.
 */
public class UniversityHeader extends UniversityGroupItem {
    private boolean isLoading;
    private ErrorEvent.ErrorType actErrorType;

    public UniversityHeader() {
        isLoading = false;
        actErrorType = null;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public ErrorEvent.ErrorType getActErrorType() {
        return actErrorType;
    }

    public void setActErrorType(ErrorEvent.ErrorType actErrorType) {
        this.actErrorType = actErrorType;
    }

    @Override
    public long getGroupId() {
        return 0;
    }
}
