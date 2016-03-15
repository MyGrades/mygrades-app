package de.mygrades.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.mygrades.R;
import de.mygrades.util.AverageCalculator;
import de.mygrades.util.Constants;
import de.mygrades.util.LogoutHelper;
import de.mygrades.view.activity.GradeDetailedActivity;
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
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;
    private boolean simpleWeighting;

    private List<GradesAdapterItem> items;

    public GradesRecyclerViewAdapter(Context context) {
        super();
        items = new ArrayList<>();

        GradesSummaryItem summary = new GradesSummaryItem();
        items.add(0, summary);
        notifyItemInserted(0);

        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(this.context);

        // register for preference changes
        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(GradesRecyclerViewAdapter.this.context.getString(R.string.pref_key_max_credit_points))) {
                    notifyItemChanged(0);
                } else if (key.equals(GradesRecyclerViewAdapter.this.context.getString(R.string.pref_key_simple_weighting))) {
                    updateSimpleWeighting();
                    updateSummary();
                    updateSemesterSummaries();
                }
            }
        };

        prefs.registerOnSharedPreferenceChangeListener(prefsListener);

        // set simple weighting
        updateSimpleWeighting();
    }

    /**
     * Add a new grade to the given semester by its termCount.
     *
     * @param newGrade - new grade
     * @param semesterNumber - semester term count
     */
    public void addGradeForSemester(GradeItem newGrade, int semesterNumber, String semester) {
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
                    if (gradeItem.getSeen() != newGrade.getSeen()) {
                        gradeItem.setSeen(newGrade.getSeen());
                        notifyItemChanged(i);
                    }

                    if (!gradeItem.equals(newGrade)) {
                        // update old grade item and notify ui
                        gradeItem.setGrade(newGrade.getGrade());
                        gradeItem.setModifiedGrade(newGrade.getModifiedGrade());
                        gradeItem.setCreditPoints(newGrade.getCreditPoints());
                        gradeItem.setModifiedCreditPoints(newGrade.getModifiedCreditPoints());
                        gradeItem.setWeight(newGrade.getWeight());
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
        semesterItem.addGrade(newGrade, true);
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
        SemesterItem newSemester = new SemesterItem(simpleWeighting);
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
        AverageCalculator calculator = new AverageCalculator(simpleWeighting);
        calculator.calculate(items);

        // get last updated at
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long timestamp = prefs.getLong(Constants.PREF_KEY_LAST_UPDATED_AT, -1);
        String lastUpdatedAt = timestampToString(timestamp);

        // if list is not empty, set summary header
        GradesSummaryItem summaryItem = (GradesSummaryItem) items.get(0);
        summaryItem.setAverage(calculator.getAverage());
        summaryItem.setCreditPoints(calculator.getCreditPointsSum());
        summaryItem.setLastUpdatedAt(lastUpdatedAt + " Uhr"); // TODO: better time format + string resource

        // show no grades info, if no grades are present
        summaryItem.setNoGradesInfoVisible(items.size() == 1);

        notifyItemChanged(0);
    }

    /**
     * Updates the summary for each SemesterItem.
     */
    private void updateSemesterSummaries() {
        for (int i = 0; i < items.size(); i++) {
            GradesAdapterItem item = items.get(i);

            if (item instanceof SemesterItem) {
                SemesterItem semesterItem = (SemesterItem) item;
                semesterItem.setSimpleWeighting(simpleWeighting);
                semesterItem.update();
                notifyItemChanged(i);
            }
        }
    }

    private void updateSimpleWeighting() {
        simpleWeighting = prefs.getBoolean(context.getString(R.string.pref_key_simple_weighting), false);
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
            GradeViewHolder gradeViewHolder = new GradeViewHolder(v);
            gradeViewHolder.setGoToDetailsClickListener(new GoToDetailsClickListener());
            return gradeViewHolder;
        } else if (viewType == VIEW_TYPE_SUMMARY) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grades_summary, parent, false);
            GradesSummaryViewHolder gradesSummaryViewHolder = new GradesSummaryViewHolder(v);
            gradesSummaryViewHolder.infoBoxMessage.setMovementMethod(LinkMovementMethod.getInstance());
            gradesSummaryViewHolder.dismissInfoBox.setOnClickListener(hideInfoBoxClickListener);
            gradesSummaryViewHolder.btnLogout.setOnClickListener(logoutClickListener);
            return gradesSummaryViewHolder;
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
            viewHolder.setGradeHash(gradeItem.getHash());
            viewHolder.tvName.setText(gradeItem.getName());

            Float grade = gradeItem.getGrade();
            String gradeAsString = grade == null ? "-" : String.format("%.1f", grade);
            viewHolder.tvGrade.setText(gradeAsString);

            Float creditPoints = gradeItem.getCreditPoints();
            String creditPointsAsString = creditPoints == null ? "-" : String.format("%.1f", creditPoints);
            viewHolder.tvCreditPoints.setText(creditPointsAsString + " CP");

            viewHolder.tvGradeSeen.setVisibility(gradeItem.getSeen() == Constants.GRADE_ENTRY_SEEN ? View.GONE : View.VISIBLE);
            String gradeSeen = "";
            if (gradeItem.getSeen() == Constants.GRADE_ENTRY_NEW) {
                gradeSeen = context.getString(R.string.tv_grade_seen_new);
            } else if (gradeItem.getSeen() == Constants.GRADE_ENTRY_UPDATED) {
                gradeSeen = context.getString(R.string.tv_grade_seen_updated);
            }
            viewHolder.tvGradeSeen.setText(gradeSeen);
        } else if (holder instanceof GradesSummaryViewHolder) {
            GradesSummaryViewHolder viewHolder = (GradesSummaryViewHolder) holder;
            GradesSummaryItem summaryItem = (GradesSummaryItem) items.get(position);

            viewHolder.tvAverage.setText(String.format("%.2f", summaryItem.getAverage()));
            viewHolder.tvCreditPoints.setText(String.format("%.1f", summaryItem.getCreditPoints()));
            viewHolder.tvLastUpdatedAt.setText(summaryItem.getLastUpdatedAt());
            viewHolder.tvMaxCreditPoints.setText(
                    context.getString(R.string.tv_max_credit_points, prefs.getString(context.getString(R.string.pref_key_max_credit_points), "")));

            // info box
            if (summaryItem.isInfoBoxVisible()) {
                viewHolder.infoBox.setVisibility(View.VISIBLE);
                viewHolder.infoBoxTitle.setText(summaryItem.getInfoBoxTitle());
                viewHolder.infoBoxMessage.setText(Html.fromHtml(summaryItem.getInfoBoxMessage()));
            } else {
                viewHolder.infoBox.setVisibility(View.GONE);
            }

            // no grades info
            if (summaryItem.isNoGradesInfoVisible()) {
                viewHolder.llNoGradesInfo.setVisibility(View.VISIBLE);
            } else {
                viewHolder.llNoGradesInfo.setVisibility(View.GONE);
            }
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
     * Shows an info box.
     *
     * @param title title
     * @param message message
     * @param dismissPrefKey preference key which should be set to true, when info box is dismissed
     */
    public void showInfoBox(String title, String message, String dismissPrefKey) {
        GradesSummaryItem summary = (GradesSummaryItem) items.get(0);
        summary.setInfoBoxTitle(title);
        summary.setInfoBoxMessage(message);
        summary.setInfoBoxVisible(true);
        summary.setDismissPrefKey(dismissPrefKey);

        notifyItemChanged(0);
    }

    /**
     * Hides the info box.
     */
    public void hideInfoBox() {
        GradesSummaryItem summary = (GradesSummaryItem) items.get(0);
        summary.setInfoBoxVisible(false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(summary.getDismissPrefKey(), true).apply();

        notifyItemChanged(0);
    }

    /**
     * Custom click listener to go to GradeDetailActivity.
     */
    private static class GoToDetailsClickListener implements View.OnClickListener {
        private String gradeHash = "";

        @Override
        public void onClick(View v) {
            final Intent intent = new Intent(v.getContext(), GradeDetailedActivity.class);
            intent.putExtra(GradeDetailedActivity.EXTRA_GRADE_HASH, gradeHash);
            v.getContext().startActivity(intent);
        }

        private void setGradeHash(String gradeHash) {
            this.gradeHash = gradeHash;
        }
    }

    /**
     * View holder for a grade entry.
     */
    public static class GradeViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout llGradeContainer;
        public TextView tvName;
        public TextView tvGrade;
        public TextView tvCreditPoints;
        public TextView tvGradeSeen;
        public GoToDetailsClickListener goToDetailsClickListener;

        public GradeViewHolder(View itemView) {
            super(itemView);

            tvName = (TextView) itemView.findViewById(R.id.tv_grade_name);
            tvGrade = (TextView) itemView.findViewById(R.id.tv_grade);
            tvCreditPoints = (TextView) itemView.findViewById(R.id.tv_credit_points);
            tvGradeSeen = (TextView) itemView.findViewById(R.id.tv_grade_seen);
            llGradeContainer = (LinearLayout) itemView.findViewById(R.id.grade_container);
        }

        public void setGoToDetailsClickListener(GoToDetailsClickListener clickListener) {
            this.goToDetailsClickListener = clickListener;
            llGradeContainer.setOnClickListener(goToDetailsClickListener);
        }

        public void setGradeHash(String gradeHash) {
            goToDetailsClickListener.setGradeHash(gradeHash);
        }
    }

    /**
     * View holder for an semester section header.
     */
    public static class SemesterViewHolder extends RecyclerView.ViewHolder {
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
     * Click listener to hide the info box.
     */
    private View.OnClickListener hideInfoBoxClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideInfoBox();
        }
    };

    /**
     * Click listener to logout.
     */
    private Button.OnClickListener logoutClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            logout();
        }
    };

    /**
     * Show the logout dialog.
     */
    private void logout() {
        LogoutHelper logoutHelper = new LogoutHelper(context);
        logoutHelper.showDialog();
    }

    /**
     * View holder for the overall summary (header).
     */
    public static class GradesSummaryViewHolder extends RecyclerView.ViewHolder {
        public TextView tvAverage;
        public TextView tvCreditPoints;
        public TextView tvLastUpdatedAt;
        public TextView tvMaxCreditPoints;
        public LinearLayout infoBox;
        public TextView infoBoxTitle;
        public TextView infoBoxMessage;
        public ImageView dismissInfoBox;
        public LinearLayout llNoGradesInfo;
        public Button btnLogout;

        public GradesSummaryViewHolder(final View itemView) {
            super(itemView);

            tvAverage = (TextView) itemView.findViewById(R.id.tv_average);
            tvCreditPoints = (TextView) itemView.findViewById(R.id.tv_credit_points);
            tvLastUpdatedAt = (TextView) itemView.findViewById(R.id.tv_last_updated_at);
            tvMaxCreditPoints = (TextView) itemView.findViewById(R.id.tv_max_credit_points);
            infoBox = (LinearLayout) itemView.findViewById(R.id.info_box);
            infoBoxMessage = (TextView) itemView.findViewById(R.id.info_box_message);
            infoBoxTitle = (TextView) itemView.findViewById(R.id.info_box_title);
            dismissInfoBox = (ImageView) itemView.findViewById(R.id.iv_dismiss_info);
            llNoGradesInfo = (LinearLayout) itemView.findViewById(R.id.ll_no_grades_wrapper);
            btnLogout = (Button) itemView.findViewById(R.id.btn_logout);
        }
    }
}
