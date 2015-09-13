package de.mygrades.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import de.mygrades.R;

/**
 * ViewHolder for an university row.
 */
public class UniversityItemViewHolder extends RecyclerView.ViewHolder {
    public final TextView universityName;

    public UniversityItemViewHolder(View rootView) {
        super(rootView);
        universityName = (TextView) rootView.findViewById(R.id.tv_university_name);
    }
}
