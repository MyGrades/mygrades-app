package de.mygrades.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Custom ImageView which clips the image by a given progress.
 */
public class ProgressImageViewOverlay extends ImageView {
    private float progress;

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

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate(); // force re draw
    }
}
