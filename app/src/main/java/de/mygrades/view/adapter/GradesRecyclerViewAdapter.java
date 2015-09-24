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
import de.mygrades.view.adapter.model.GradeItem;
import de.mygrades.view.adapter.model.GradesAdapterItem;
import de.mygrades.view.adapter.model.SemesterItem;

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

    /**
     * Add a new grade to the given semester by its termCount.
     *
     * @param newGrade - new grade
     * @param termCount - semester term count
     */
    public void addGradeForSemester(GradeItem newGrade, int termCount) {
        // find semester index, where the grade should be added
        int semesterIndex = getIndexForSemester(termCount);

        // find position in semester (lexicographic)
        // start after the semesterIndex
        int newGradeIndex = items.size();
        for(int i = semesterIndex + 1; i < items.size(); i++) {
            if (items.get(i) instanceof GradeItem) {
                GradeItem gradeItem = (GradeItem) items.get(i);
                if (newGrade.getName().compareToIgnoreCase(gradeItem.getName()) <= 0) {
                    newGradeIndex = i;
                    break;
                }
            } else if (items.get(i) instanceof SemesterItem) {
                newGradeIndex = i;
                break;
            }
        }

        // add grade to semester and update semester header
        SemesterItem semester = (SemesterItem) items.get(semesterIndex);
        semester.addGrade(newGrade);
        notifyItemChanged(semesterIndex);

        // add grade to the item list
        items.add(newGradeIndex, newGrade);
        notifyItemInserted(newGradeIndex);
    }

    /**
     * Get the index for a semester by its termCount.
     *
     * @param termCount - term count
     * @return index
     */
    private int getIndexForSemester(int termCount) {
        // find the index for a semester
        int semesterIndex = -1;
        for(int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof SemesterItem) {
                SemesterItem semesterItem = (SemesterItem) items.get(i);
                if (semesterItem.getTermCount() == termCount) {
                    semesterIndex = i;
                    break;
                }
            }
        }

        // no semester found, add a new one
        if (semesterIndex < 0) {
            semesterIndex = addSemester(termCount);
        }

        return semesterIndex;
    }

    /**
     * Adds a new semester to the item list.
     * Semester are sorted descend by its term count (semester 3, semester 2, semester 1)
     *
     * @param termCount - term count
     * @return index
     */
    private int addSemester(int termCount) {
        int semesterIndex = -1; // -1, if there are no semesters so far

        // find index where the semester should be added (descending by term count)
        for(int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof SemesterItem) {
                SemesterItem semesterItem = (SemesterItem) items.get(i);
                if (termCount > semesterItem.getTermCount()) {
                    semesterIndex = i;
                    break;
                }
            }
        }

        // if list is empty, use index 0
        semesterIndex = items.size() == 0 ? 0 : semesterIndex;

        // if new semester goes to bottom, use item.size()
        semesterIndex = semesterIndex < 0 ? items.size() : semesterIndex;

        // add new semester
        SemesterItem newSemester = new SemesterItem();
        newSemester.setTermCount(termCount);
        items.add(semesterIndex, newSemester);
        notifyItemInserted(semesterIndex);

        return semesterIndex;
    }

    /**
     * Clears all items.
     */
    public void clear() {
        notifyItemRangeRemoved(0, items.size());
        items.clear();
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
            viewHolder.tvAverage.setText("Ø " + String.format("%.2f", semesterItem.getAverage()));
            viewHolder.tvCreditPoints.setText("Σ " + String.format("%.1f", semesterItem.getCreditPoints()) + " CP");
        } else if (holder instanceof GradeViewHolder) {
            GradeViewHolder viewHolder = (GradeViewHolder) holder;
            GradeItem gradeItem = (GradeItem) items.get(position);

            viewHolder.tvName.setText(gradeItem.getName());
            viewHolder.tvGrade.setText("" + String.format("%.1f", gradeItem.getGrade()));
            viewHolder.tvCreditPoints.setText("" + String.format("%.1f", gradeItem.getCreditPoints()) + " CP");
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
