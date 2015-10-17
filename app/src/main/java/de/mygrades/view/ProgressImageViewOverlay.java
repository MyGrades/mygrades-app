package de.mygrades.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Custom ImageView which clips the image by a given progress.
 * For example a progress of 0.3f will only show the lower 30% of the canvas.
 */
public class ProgressImageViewOverlay extends ImageView {
    private float progress;
    private float nextProgress;

    public ProgressImageViewOverlay(Context context) {
        super(context);
    }

    public ProgressImageViewOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgressImageViewOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int top = (int) (getBottom() * (1f - progress));

        canvas.clipRect(getLeft(), top, getRight(), getBottom());
        super.onDraw(canvas);
    }

    /**
     * Set the progress and re-draw.
     *
     * @param progress - current progress
     * @param nextProgress - next progress is used as a boundary for the animation
     */
    public void setProgress(float progress, float nextProgress) {
        this.progress = progress;
        this.nextProgress = nextProgress;
        invalidate(); // force re-draw
    }

    /**
     * Increase the progress and re-draw.
     *
     * @param increase amount to increase
     */
    public void increaseProgress(float increase) {
        if (this.progress + increase <= nextProgress) {
            this.progress += increase;
            invalidate(); // force re-draw
        }
    }

    public float getProgress() {
        return progress;
    }

    public float getNextProgress() {
        return nextProgress;
    }
}
