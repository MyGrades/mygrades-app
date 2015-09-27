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
import de.mygrades.view.adapter.model.GradesAdapterItem;
import de.mygrades.view.adapter.model.GradesSummaryItem;
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
    public void addGradeForSemester(GradeItem newGrade, int semesterNumber, String semester) {
        // add summary if necessary
        if (items.size() == 0) {
            // always add summary to top
            GradesSummaryItem summary = new GradesSummaryItem();
            items.add(0, summary);
            notifyItemInserted(0);
        }

        // find semester index, where the grade should be added
        int semesterIndex = getIndexForSemester(semesterNumber, semester);

        // update grade if necessary
        if (!updateGrade(newGrade, semesterIndex)) {
            addGrade(newGrade, semesterIndex);
        }
    }

    /**
     * Checks if the new grade is already in the list and updates it if necessary.
     *
     * @param newGrade - new grade
     * @param semesterIndex - semester index
     * @return true, if grade was updated or if it exists already. false otherwise
     */
    private boolean updateGrade(GradeItem newGrade, int semesterIndex) {

        for(int i = semesterIndex + 1; i < items.size(); i++) {
            if (items.get(i) instanceof GradeItem) {
                GradeItem gradeItem = (GradeItem) items.get(i);
                if (gradeItem.getHash().equals(newGrade.getHash())) {
                    if (!gradeItem.equals(newGrade)) {
                        // update old grade item and notify ui
                        gradeItem.setGrade(newGrade.getGrade());
                        gradeItem.setCreditPoints(newGrade.getCreditPoints());
                        notifyItemChanged(i);

                        // update semester item and notify ui
                        if (items.get(semesterIndex) instanceof SemesterItem) {
                            SemesterItem semesterItem = (SemesterItem) items.get(semesterIndex);
                            semesterItem.update();
                            notifyItemChanged(semesterIndex);
                        }
                    }
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Adds a new grade after the given semester index.
     *
     * @param newGrade - new grade to add
     * @param semesterIndex - semester index
     */
    private void addGrade(GradeItem newGrade, int semesterIndex) {
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
        SemesterItem semesterItem = (SemesterItem) items.get(semesterIndex);
        semesterItem.addGrade(newGrade);
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
    private int getIndexForSemester(int semesterNumber, String semester) {
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
            semesterIndex = addSemester(semesterNumber, semester);
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
    private int addSemester(int semesterNumber, String semester) {
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
        newSemester.setSemester(semester);
        items.add(semesterIndex, newSemester);
        notifyItemInserted(semesterIndex);

        return semesterIndex;
    }

    /**
     * Update the summary and set average, creditPoints and lastUpdatedAt.
     */
    public void updateSummary() {
        float average = 0f;
        float creditPointsSum = 0f;
        float creditPointsSumForAverage = 0f; // sum grade_entries may have credit points, but no grade

        // iterate over items, count credit points and calculate average
        for(GradesAdapterItem item : items) {
            if (item instanceof GradeItem) {
                GradeItem grade = (GradeItem) item;
                float actCreditPoints = (grade.getCreditPoints() == null ? 0f : grade.getCreditPoints());
                creditPointsSum += actCreditPoints;
                if (grade.getGrade() != null && actCreditPoints > 0) {
                    creditPointsSumForAverage += actCreditPoints;
                }
                average += (grade.getGrade() == null ? 0f : grade.getGrade() * actCreditPoints);
            }
        }
        average = creditPointsSumForAverage > 0 ? average/creditPointsSumForAverage : 0f;

        // get last updated at
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long timestamp = prefs.getLong(Constants.PREF_KEY_LAST_UPDATED_AT, -1);
        String lastUpdatedAt = timestampToString(timestamp);

        // if list is not empty, set summary header
        if (items.size() > 0) {
            GradesSummaryItem summaryItem = (GradesSummaryItem) items.get(0);
            summaryItem.setAverage(average);
            summaryItem.setCreditPoints(creditPointsSum);
            summaryItem.setLastUpdatedAt(lastUpdatedAt + " Uhr"); // TODO: better time format + string resource
            notifyItemChanged(0);
        }
    }

    /**
     * Convert a given timestamp to a string representation.
     *
     * @param timestamp - timestamp to convert
     * @return timestamp as string
     */
    public static String timestampToString(long timestamp) {
        if (timestamp < 0) {
            return "-";
        }

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
            return new GradesSummaryViewHolder(v);
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
        } else if (holder instanceof GradesSummaryViewHolder) {
            GradesSummaryViewHolder viewHolder = (GradesSummaryViewHolder) holder;
            GradesSummaryItem summaryItem = (GradesSummaryItem) items.get(position);

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
        } else if (items.get(position) instanceof GradesSummaryItem) {
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
    public class GradesSummaryViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAverage;
        public TextView tvCreditPoints;
        public TextView tvLastUpdatedAt;

        public GradesSummaryViewHolder(View itemView) {
            super(itemView);

            tvAverage = (TextView) itemView.findViewById(R.id.tv_average);
            tvCreditPoints = (TextView) itemView.findViewById(R.id.tv_credit_points);
            tvLastUpdatedAt = (TextView) itemView.findViewById(R.id.tv_last_updated_at);
        }
    }
}
