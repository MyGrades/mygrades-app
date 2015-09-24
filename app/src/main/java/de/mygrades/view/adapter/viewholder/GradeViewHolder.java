package de.mygrades.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.mygrades.R;

/**
 * View holder for a grade entry.
 */
public class GradeViewHolder extends RecyclerView.ViewHolder {
    public TextView tvName;
    public TextView tvGrade;
    public TextView tvCreditPoints;

    public GradeViewHolder(View itemView) {
        super(itemView);

        tvName = (TextView) itemView.findViewById(R.id.tv_grade_name);
        tvGrade = (TextView) itemView.findViewById(R.id.tv_grade);
        tvCreditPoints = (TextView) itemView.findViewById(R.id.tv_credit_points);
    }
}
