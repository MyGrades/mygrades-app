package de.mygrades.view.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import com.twotoasters.sectioncursoradapter.adapter.viewholder.ViewHolder;

/**
 * ViewHolder for an university row.
 */
public class UniversityItemViewHolder extends ViewHolder {
    public final TextView universityName;

    public UniversityItemViewHolder(View rootView) {
        super(rootView);
        universityName = (TextView) rootView;
    }
}
