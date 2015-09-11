package de.mygrades.view.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import com.twotoasters.sectioncursoradapter.adapter.viewholder.ViewHolder;

import de.mygrades.R;

/**
 * ViewHolder for the section title. (A, B, C...)
 */
public class UniversitySectionViewHolder extends ViewHolder {
    public final TextView sectionTitle;

    public UniversitySectionViewHolder(View rootView) {
        super(rootView);
        sectionTitle = (TextView) rootView.findViewById(R.id.tv_university_section);
    }
}
