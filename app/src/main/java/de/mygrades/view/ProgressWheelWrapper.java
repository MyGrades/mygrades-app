package de.mygrades.view;

import android.content.Context;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.pnikosis.materialishprogress.ProgressWheel;

import de.mygrades.R;

/**
 * Wrapper Object with convenience methods for ProgressWheel.
 */
public class ProgressWheelWrapper {
    private static final float DEFAULT_PROGRESS = 0.025f;

    private ProgressWheel progressWheel;
    private View background;

    public ProgressWheelWrapper(ProgressWheel progressWheel, View background) {
        this.progressWheel = progressWheel;
        this.background = background;
    }

    /**
     * Resets the animation and the progressWheel.
     */
    public void reset() {
        progressWheel.setProgress(DEFAULT_PROGRESS);
        background.setVisibility(View.INVISIBLE);
    }

    /**
     * Starts animation of the progressWheel.
     * @param context
     */
    public void startAnimation(Context context) {
        progressWheel.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate_indefinitely));
    }

    /**
     * Starts finish animation of background.
     * @param context Context
     */
    public void loadingFinished(Context context) {
        progressWheel.setAnimation(null);
        background.startAnimation(AnimationUtils.loadAnimation(context, R.anim.abc_grow_fade_in_from_bottom));
        background.setVisibility(View.VISIBLE);
    }

    /**
     * Increases Progress of the progressWheel.
     * @param currentStep current step of loading
     * @param stepCount count of all steps of loading
     */
    public void increaseProgress(int currentStep, int stepCount) {
        float progress = ((float) currentStep) / stepCount;
        setProgress(progress);
    }

    /**
     * Sets Progress of the progressWheel.
     * @param progress progress to set
     */
    public void setProgress(float progress) {
        progress = progress == 0 ? DEFAULT_PROGRESS : progress;
        progressWheel.setProgress(progress);
    }

    /**
     * Gets Progress of the progressWheel.
     * @return progess
     */
    public float getProgress() {
        return progressWheel.getProgress();
    }
}
