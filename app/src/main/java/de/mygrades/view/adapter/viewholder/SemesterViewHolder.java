package de.mygrades.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.mygrades.R;

/**
 * View holder for an semester section header.
 */
public class SemesterViewHolder extends RecyclerView.ViewHolder {
    public TextView tvTermCount;
    public TextView tvTermAsString;
    public TextView tvAverage;
    public TextView tvCreditPoints;

    public SemesterViewHolder(View itemView) {
        super(itemView);

        tvTermCount = (TextView) itemView.findViewById(R.id.tv_term_count);
        tvTermAsString = (TextView) itemView.findViewById(R.id.tv_semester);
        tvAverage = (TextView) itemView.findViewById(R.id.tv_average);
        tvCreditPoints = (TextView) itemView.findViewById(R.id.tv_credit_points);
    }
}
