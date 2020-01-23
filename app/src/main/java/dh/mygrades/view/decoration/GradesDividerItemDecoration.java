package dh.mygrades.view.decoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import dh.mygrades.view.adapter.GradesRecyclerViewAdapter;

/**
 * Custom divider item decoration for the overview of grades.
 */
public class GradesDividerItemDecoration extends RecyclerView.ItemDecoration  {

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    private Drawable gradeDivider;
    private Drawable semesterDivider;

    /**
     * Default divider will be used
     */
    public GradesDividerItemDecoration(Context context) {
        final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        gradeDivider = styledAttributes.getDrawable(0);
        styledAttributes.recycle();
    }

    /**
     * Custom divider will be used
     */
    public GradesDividerItemDecoration(Context context, int gradeDividerResId, int semesterDividerResId) {
        gradeDivider = ContextCompat.getDrawable(context, gradeDividerResId);
        semesterDivider = ContextCompat.getDrawable(context, semesterDividerResId);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            Drawable divider;
            if (parent.getChildViewHolder(child) instanceof GradesRecyclerViewAdapter.SemesterViewHolder) {
                divider = semesterDivider;
            } else {
                divider = gradeDivider;
            }

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin + (int) child.getTranslationY();
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.setAlpha((int) (child.getAlpha() * 255));
            divider.draw(c);
        }
    }
}
