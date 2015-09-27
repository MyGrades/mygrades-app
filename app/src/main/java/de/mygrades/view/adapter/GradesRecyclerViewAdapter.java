package de.mygrades.view.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.mygrades.R;
import de.mygrades.util.Constants;
import de.mygrades.view.adapter.model.GradeItem;
import de.mygrades.view.adapter.model.GradeSummaryItem;
import de.mygrades.view.adapter.model.GradesAdapterItem;
import de.mygrades.view.adapter.model.SemesterItem;

/**
 * Custom RecyclerView adapter for the overview of grades with semester headers.
 */
public class GradesRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_SEMESTER = 0;
    private final int VIEW_TYPE_GRADE = 1;
    private final int VIEW_TYPE_SUMMARY = 2;

    private Context context;
    private List<GradesAdapterItem> items;

    public GradesRecyclerViewAdapter(Context context) {
        super();
        items = new ArrayList<>();
        this.context = context;
    }

    /**
     * Add a new grade to the given semester by its termCount.
     *
     * @param newGrade - new grade
     * @param semesterNumber - semester term count
     */
    public void addGradeForSemester(GradeItem newGrade, int semesterNumber) {
        // add summary if necessary
        if (items.size() == 0) {
            // always add summary to top
            GradeSummaryItem summary = new GradeSummaryItem();
            items.add(0, summary);
            notifyItemInserted(0);
        }

        // find semester index, where the grade should be added
        int semesterIndex = getIndexForSemester(semesterNumber);

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
     * @param semesterNumber - term count
     * @return index
     */
    private int getIndexForSemester(int semesterNumber) {
        // find the index for a semester
        int semesterIndex = -1;
        for(int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof SemesterItem) {
                SemesterItem semesterItem = (SemesterItem) items.get(i);
                if (semesterItem.getSemesterNumber() == semesterNumber) {
                    semesterIndex = i;
                    break;
                }
            }
        }

        // no semester found, add a new one
        if (semesterIndex < 0) {
            semesterIndex = addSemester(semesterNumber);
        }

        return semesterIndex;
    }

    /**
     * Adds a new semester to the item list.
     * Semester are sorted descend by its term count (semester 3, semester 2, semester 1)
     *
     * @param semesterNumber - semester number
     * @return index
     */
    private int addSemester(int semesterNumber) {
        int semesterIndex = items.size(); // add to bottom , if no other position will be found

        // find index where the semester should be added (descending by semester number)
        for(int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof SemesterItem) {
                SemesterItem semesterItem = (SemesterItem) items.get(i);
                if (semesterNumber > semesterItem.getSemesterNumber()) {
                    semesterIndex = i;
                    break;
                }
            }
        }

        // add new semester
        SemesterItem newSemester = new SemesterItem();
        newSemester.setSemesterNumber(semesterNumber);
        items.add(semesterIndex, newSemester);
        notifyItemInserted(semesterIndex);

        return semesterIndex;
    }

    public void updateSummary() {
        float average = 0f;
        float creditPointsSum = 0f;

        // iterate over items, count credit points and calculate average
        for(GradesAdapterItem item : items) {
            if (item instanceof GradeItem) {
                GradeItem grade = (GradeItem) item;
                float actCreditPoints = (grade.getCreditPoints() == null ? 0f : grade.getCreditPoints());
                creditPointsSum += actCreditPoints;
                average += (grade.getGrade() == null ? 0f : grade.getGrade() * actCreditPoints);
            }
        }
        average /= creditPointsSum;

        // get last updated at
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long timestamp = prefs.getLong(Constants.PREF_KEY_LAST_UPDATED_AT, -1);
        String lastUpdatedAt = timestampToString(timestamp);

        GradeSummaryItem summaryItem = (GradeSummaryItem) items.get(0);
        summaryItem.setAverage(average);
        summaryItem.setCreditPoints(creditPointsSum);
        summaryItem.setLastUpdatedAt(lastUpdatedAt + " Uhr"); // TODO: better time format + string resource
        notifyItemChanged(0);
    }

    public static String timestampToString(long timestamp) {
        if (timestamp < 0) {
            return "-";
        }

        //timestamp *= 1000;
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date date = new Date(timestamp);
        return df.format(date);
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
        } else if (viewType == VIEW_TYPE_SUMMARY) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grades_summary, parent, false);
            return new GradeSummaryViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SemesterViewHolder) {
            SemesterViewHolder viewHolder = (SemesterViewHolder) holder;
            SemesterItem semesterItem = (SemesterItem) items.get(position);

            viewHolder.tvSemesterNumber.setText("" + semesterItem.getSemesterNumber() + ". Semester");
            viewHolder.tvSemester.setText(semesterItem.getSemester());
            viewHolder.tvAverage.setText("Ø " + String.format("%.2f", semesterItem.getAverage()));
            viewHolder.tvCreditPoints.setText("Σ " + String.format("%.1f", semesterItem.getCreditPoints()) + " CP");
        } else if (holder instanceof GradeViewHolder) {
            GradeViewHolder viewHolder = (GradeViewHolder) holder;
            GradeItem gradeItem = (GradeItem) items.get(position);

            viewHolder.tvName.setText(gradeItem.getName());

            Float grade = gradeItem.getGrade();
            String gradeAsString = grade == null ? "-" : String.format("%.1f", grade);
            viewHolder.tvGrade.setText(gradeAsString);

            Float creditPoints = gradeItem.getCreditPoints();
            String creditPointsAsString = creditPoints == null ? "-" : String.format("%.1f", creditPoints);
            viewHolder.tvCreditPoints.setText(creditPointsAsString + " CP");
        } else if (holder instanceof GradeSummaryViewHolder) {
            GradeSummaryViewHolder viewHolder = (GradeSummaryViewHolder) holder;
            GradeSummaryItem summaryItem = (GradeSummaryItem) items.get(position);

            viewHolder.tvAverage.setText(String.format("%.2f", summaryItem.getAverage()));
            viewHolder.tvCreditPoints.setText(String.format("%.1f", summaryItem.getCreditPoints()));
            viewHolder.tvLastUpdatedAt.setText(summaryItem.getLastUpdatedAt());
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
        } else if (items.get(position) instanceof GradeSummaryItem) {
            return VIEW_TYPE_SUMMARY;
        }

        return -1;
    }

    /**
     * View holder for a grade entry.
     */
    public class GradeViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvGrade;
        public TextView tvCreditPoints;

        public GradeViewHolder(View itemView) {
            super(itemView);

            tvName = (TextView) itemView.findViewById(R.id.tv_grade_name);
            tvGrade = (TextView) itemView.findViewById(R.id.tv_grade);
            tvCreditPoints = (TextView) itemView.findViewById(R.id.tv_credit_points);
        }
    }

    /**
     * View holder for an semester section header.
     */
    public class SemesterViewHolder extends RecyclerView.ViewHolder {
        public TextView tvSemesterNumber;
        public TextView tvSemester;
        public TextView tvAverage;
        public TextView tvCreditPoints;

        public SemesterViewHolder(View itemView) {
            super(itemView);

            tvSemesterNumber = (TextView) itemView.findViewById(R.id.tv_semester_number);
            tvSemester = (TextView) itemView.findViewById(R.id.tv_semester);
            tvAverage = (TextView) itemView.findViewById(R.id.tv_average);
            tvCreditPoints = (TextView) itemView.findViewById(R.id.tv_credit_points);
        }
    }

    /**
     * View holder for the overall summary (header).
     */
    public class GradeSummaryViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAverage;
        public TextView tvCreditPoints;
        public TextView tvLastUpdatedAt;

        public GradeSummaryViewHolder(View itemView) {
            super(itemView);

            tvAverage = (TextView) itemView.findViewById(R.id.tv_average);
            tvCreditPoints = (TextView) itemView.findViewById(R.id.tv_credit_points);
            tvLastUpdatedAt = (TextView) itemView.findViewById(R.id.tv_last_updated_at);
        }
    }
}
