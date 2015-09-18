package de.mygrades.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import de.mygrades.R;

/**
 * ViewHolder for an university section title.
 */
public class UniversitySectionViewHolder extends RecyclerView.ViewHolder {
    public final TextView sectionTitle;

    public UniversitySectionViewHolder(View rootView) {
        super(rootView);
        sectionTitle = (TextView) rootView.findViewById(R.id.tv_section_title);
    }
}
