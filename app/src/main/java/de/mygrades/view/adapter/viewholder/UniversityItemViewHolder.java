package de.mygrades.view.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import com.twotoasters.sectioncursoradapter.adapter.viewholder.ViewHolder;

import de.mygrades.R;

/**
 * ViewHolder for an university row.
 */
public class UniversityItemViewHolder extends ViewHolder {
    public final TextView universityName;

    public UniversityItemViewHolder(View rootView) {
        super(rootView);
        universityName = (TextView) rootView.findViewById(R.id.tv_university_name);
    }
}
