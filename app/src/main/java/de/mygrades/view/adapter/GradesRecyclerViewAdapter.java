package de.mygrades.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.R;
import de.mygrades.view.adapter.viewholder.GradeViewHolder;
import de.mygrades.view.adapter.viewholder.SemesterViewHolder;
import de.mygrades.view.model.GradeItem;
import de.mygrades.view.model.GradesAdapterItem;
import de.mygrades.view.model.SemesterItem;

/**
 * Custom RecyclerView adapter for the overview of grades with semester headers.
 */
public class GradesRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_SEMESTER = 0;
    private final int VIEW_TYPE_GRADE = 1;

    private List<GradesAdapterItem> items;

    public GradesRecyclerViewAdapter() {
        super();
        items = new ArrayList<>();
    }

    public void add(GradesAdapterItem item, int position) {
        items.add(position, item);
        notifyItemInserted(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SEMESTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_semester, parent, false);
            return new SemesterViewHolder(v);
        } else if (viewType == VIEW_TYPE_GRADE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grade, parent, false);
            return new GradeViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SemesterViewHolder) {
            SemesterViewHolder viewHolder = (SemesterViewHolder) holder;
            SemesterItem semesterItem = (SemesterItem) items.get(position);

            viewHolder.tvTermCount.setText("" + semesterItem.getTermCount() + ". Semester");
            viewHolder.tvTermAsString.setText(semesterItem.getTermAsString());
            viewHolder.tvAverage.setText("Ø " + semesterItem.getAverage());
            viewHolder.tvCreditPoints.setText("Σ " + semesterItem.getCreditPoints() + " CP");
        } else if (holder instanceof GradeViewHolder) {
            GradeViewHolder viewHolder = (GradeViewHolder) holder;
            GradeItem gradeItem = (GradeItem) items.get(position);

            viewHolder.tvName.setText(gradeItem.getName());
            viewHolder.tvGrade.setText("" + gradeItem.getGrade());
            viewHolder.tvCreditPoints.setText("" + gradeItem.getCreditPoints() + " CP");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof  SemesterItem) {
            return VIEW_TYPE_SEMESTER;
        } else if (items.get(position) instanceof GradeItem) {
            return VIEW_TYPE_GRADE;
        }

        return -1;
    }
}
