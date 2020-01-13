package dh.mygrades.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import dh.mygrades.R;

/**
 * Draws a simple triangle badge.
 */
public class TriangleShapeView extends View {
    private Path path;
    private Paint paint;

    public TriangleShapeView(Context context) {
        super(context);
        create();
    }

    public TriangleShapeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        create();
    }

    public TriangleShapeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        create();
    }

    private void create() {
        paint = new Paint();
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        paint.setStyle(Paint.Style.FILL);
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth() / 2;

        path.rewind();
        path.moveTo(w, 0);
        path.lineTo(2 * w, 0);
        path.lineTo(2 * w, w);
        path.lineTo(w, 0);
        path.close();

        canvas.drawPath(path, paint);
    }
}
