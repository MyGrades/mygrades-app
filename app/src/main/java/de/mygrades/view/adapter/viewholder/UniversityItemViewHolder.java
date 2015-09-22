package de.mygrades.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.mygrades.R;

/**
 * ViewHolder for an university row.
 */
public class UniversityItemViewHolder extends RecyclerView.ViewHolder {
    public final TextView tvUniversityName;

    public final LinearLayout llSectionHeader;
    public final TextView tvSectionHeader;

    public UniversityItemViewHolder(View rootView) {
        super(rootView);
        tvUniversityName = (TextView) rootView.findViewById(R.id.tv_university_name);
        llSectionHeader = (LinearLayout) rootView.findViewById(R.id.ll_section_header);
        tvSectionHeader = (TextView) rootView.findViewById(R.id.tv_section_title);
    }
}
