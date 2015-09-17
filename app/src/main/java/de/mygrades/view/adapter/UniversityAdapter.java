package de.mygrades.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.mygrades.R;
import de.mygrades.view.adapter.viewholder.UniversityItemViewHolder;

/**
 * Adapter for university list.
 */
public class UniversityAdapter extends CursorRecyclerViewAdapter {


    public UniversityAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        int columnIndex = -1; //cursor.getColumnIndex(Database.University.NAME);
        String name = cursor.getString(columnIndex);

        // set university name
        UniversityItemViewHolder universityItemViewHolder = (UniversityItemViewHolder) viewHolder;
        universityItemViewHolder.universityName.setText(name);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_university, parent, false);
        RecyclerView.ViewHolder vh = new UniversityItemViewHolder(itemView);
        return vh;
    }
}
