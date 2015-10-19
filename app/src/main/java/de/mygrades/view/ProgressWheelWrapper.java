package de.mygrades.view;

import android.content.Context;
import android.view.animation.AnimationUtils;

import com.pnikosis.materialishprogress.ProgressWheel;

import de.mygrades.R;

/**
 * Wrapper Object with convenience methods for ProgressWheel.
 */
public class ProgressWheelWrapper {
    private static final float DEFAULT_PROGRESS = 0.025f;

    private ProgressWheel progressWheel;

    public ProgressWheelWrapper(ProgressWheel progressWheel) {
        this.progressWheel = progressWheel;
    }

    public void reset() {
        progressWheel.setProgress(DEFAULT_PROGRESS);
    }

    public void startAnimation(Context context) {
        progressWheel.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate_indefinitely));
    }

    public void endAnimation() {
        progressWheel.setAnimation(null);
    }

    public void increaseProgress(int currentStep, int stepCount) {
        float progress = ((float) currentStep) / stepCount;
        progress = progress == 0 ? DEFAULT_PROGRESS : progress;
        progressWheel.setProgress(progress);
    }
}
