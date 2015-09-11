package de.mygrades.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

import com.twotoasters.sectioncursoradapter.adapter.SectionCursorAdapter;

import de.mygrades.R;
import de.mygrades.database.Database;
import de.mygrades.view.adapter.viewholder.UniversityItemViewHolder;
import de.mygrades.view.adapter.viewholder.UniversitySectionViewHolder;

/**
 * Adapter for university list with sections (A, B, C...)
 */
public class UniversityAdapter extends SectionCursorAdapter<String, UniversitySectionViewHolder, UniversityItemViewHolder> {

    public UniversityAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0, R.layout.item_university_section, R.layout.item_university);
    }

    @Override
    protected String getSectionFromCursor(Cursor cursor) throws IllegalStateException {
        int columnIndex = cursor.getColumnIndex(Database.University.NAME);
        String name = cursor.getString(columnIndex);
        return name.toUpperCase().substring(0, 1);
    }

    @Override
    protected UniversitySectionViewHolder createSectionViewHolder(View view, String s) {
        return new UniversitySectionViewHolder(view);
    }

    @Override
    protected void bindSectionViewHolder(int i, UniversitySectionViewHolder universitySectionViewHolder, ViewGroup viewGroup, String section) {
        universitySectionViewHolder.sectionTitle.setText(section);
    }

    @Override
    protected UniversityItemViewHolder createItemViewHolder(Cursor cursor, View view) {
        return new UniversityItemViewHolder(view);
    }

    @Override
    protected void bindItemViewHolder(UniversityItemViewHolder universityItemViewHolder, Cursor cursor, ViewGroup viewGroup) {
        int columnIndex = cursor.getColumnIndex(Database.University.NAME);
        String name = cursor.getString(columnIndex);
        universityItemViewHolder.universityName.setText(name);
    }
}
