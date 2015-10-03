package de.mygrades.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by tilman on 03.10.15.
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
        int left = getLeft();
        int right = getRight();
        int top = (int) (getTop() + getBottom() * (1f - progress));
        int bottom = (int) (getBottom() * 1f);

        Log.v(".....", "draw " + progress);

        canvas.clipRect(left, top, right, bottom);
        super.onDraw(canvas);
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }
}
