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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.util.AverageCalculator;
import de.mygrades.util.Constants;
import de.mygrades.util.LogoutHelper;
import de.mygrades.view.activity.GradeDetailedActivity;
import de.mygrades.view.adapter.model.GradeItem;
import de.mygrades.view.adapter.model.GradesAdapterItem;
import de.mygrades.view.adapter.model.GradesFooterItem;
import de.mygrades.view.adapter.model.GradesSummaryItem;
import de.mygrades.view.adapter.model.SemesterItem;
import de.mygrades.view.widget.TriangleShapeView;

/**
 * Custom RecyclerView adapter for the overview of grades with semester headers.
 */
public class GradesRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_SEMESTER = 0;
    private final int VIEW_TYPE_GRADE = 1;
    private final int VIEW_TYPE_SUMMARY = 2;
    private final int VIEW_TYPE_FOOTER = 3;

    private Context context;
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;
    private boolean simpleWeighting;

    private List<GradesAdapterItem> items;
    private Map<String, Integer> semesterNumberMap;
    private String actualFirstSemester;

    private boolean editModeEnabled;

    public GradesRecyclerViewAdapter(Context context) {
        super();
        items = new ArrayList<>();
        semesterNumberMap = new HashMap<>();

        GradesSummaryItem summary = new GradesSummaryItem();
        GradesFooterItem footer = new GradesFooterItem(false);
        items.add(0, summary);
        items.add(1, footer);
        notifyItemRangeInserted(0, 1);

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
     */
    public void addGrade(GradeItem newGrade) {
        int semesterNumber = semesterNumberMap.get(newGrade.getCurrentSemester());
        String semester = newGrade.getCurrentSemester();

        // check if grade already exists in different semester and delete it
        checkForDeletion(newGrade);

        // find semester index, where the grade should be added
        int semesterIndex = getIndexForSemester(semesterNumber, semester);

        // update grade if necessary
        if (!updateGrade(newGrade, semesterIndex)) {
            addGrade(newGrade, semesterIndex);
        }

        updateFooter();
    }

    /**
     * Show footer if grade items exist.
     */
    private void updateFooter() {
        if (items.size() > 1) {
            ((GradesFooterItem)items.get(items.size() - 1)).setVisible(items.size() > 2);
            notifyItemChanged(items.size() - 1);
        }
    }

    /**
     * Deletes a grade item and its semester if necessary.
     *
     * @param newGrade grade item to delete
     */
    public void deleteGrade(GradeItem newGrade) {
        int semesterNumber = semesterNumberMap.get(newGrade.getCurrentSemester());
        String semester = newGrade.getCurrentSemester();

        // find semester index, where the grade should be added
        int semesterIndex = getIndexForSemester(semesterNumber, semester);
        SemesterItem semesterItem = (SemesterItem) items.get(semesterIndex);

        // find grade item index
        int indexToDelete = -1;
        for(int i = semesterIndex + 1; i < items.size() - 1; i++) {
            if (items.get(i) instanceof GradeItem) {
                GradeItem gradeItem = (GradeItem) items.get(i);
                if (gradeItem.getHash().equals(newGrade.getHash())) {
                    indexToDelete = i;
                    break;
                }
            }
        }

        if (indexToDelete > 0 && semesterIndex > 0) {
            GradeItem gradeItemToDelete = (GradeItem) items.get(indexToDelete);
            removeGrade(gradeItemToDelete, indexToDelete, semesterItem, semesterIndex);

            // check if any semester is now empty and delete it if necessary
            deleteEmptySemester();
        }

        updateSummary();
        updateFooter();
    }

    /**
     * Checks if the grade item exists in another semester or under another name and removes it,
     * so it can be added again at the correct position.
     *
     * @param newGrade new grade item
     */
    private void checkForDeletion(GradeItem newGrade) {
        int currentSemesterIndex = -1;
        SemesterItem currentSemesterItem = null;

        for (int i = 0; i < items.size() - 1; i++) {
            if (items.get(i) instanceof SemesterItem) {
                currentSemesterIndex = i;
                currentSemesterItem = (SemesterItem) items.get(i);
            }

            if (items.get(i) instanceof GradeItem) {
                GradeItem gradeItem = (GradeItem) items.get(i);
                if (gradeItem.getHash().equals(newGrade.getHash())) {

                    // check if semester has changed and remove old grade item if necessary
                    Integer oldSemesterNumber = semesterNumberMap.get(gradeItem.getCurrentSemester());
                    Integer newSemesterNumber = semesterNumberMap.get(newGrade.getCurrentSemester());
                    if (!oldSemesterNumber.equals(newSemesterNumber)) {
                        removeGrade(gradeItem, i, currentSemesterItem, currentSemesterIndex);
                        return;
                    }

                    // check if shown name has changed
                    if (!newGrade.getShownName().equals(gradeItem.getShownName())) {
                        removeGrade(gradeItem, i, currentSemesterItem, currentSemesterIndex);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Removes a GradeItem at the given index and semester and notifies the item changes.
     *
     * @param gradeItem grade item to remove
     * @param gradeIndex grade item index
     * @param semesterItem semester item, to which the grade item belongs
     * @param semesterIndex semester item index
     */
    private void removeGrade(GradeItem gradeItem, int gradeIndex, SemesterItem semesterItem, int semesterIndex) {
        // remove old grade and update ui
        if (semesterItem != null) {
            semesterItem.getGrades().remove(gradeItem);
            semesterItem.update();
            if (semesterIndex > 0) {
                notifyItemChanged(semesterIndex);
            }
        }

        items.remove(gradeIndex);
        notifyItemRemoved(gradeIndex);
    }

    /**
     * Checks if any semester is empty and deletes it.
     */
    private void deleteEmptySemester() {
        for (int i = 0; i < items.size() - 1; i++) {
            if (items.get(i) instanceof SemesterItem) {
                SemesterItem semesterItem = (SemesterItem) items.get(i);
                if (semesterItem.getGrades().size() == 0) {
                    items.remove(i);
                    notifyItemRemoved(i);
                    return;
                }
            }
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
        for(int i = semesterIndex + 1; i < items.size() - 1; i++) {
            if (items.get(i) instanceof GradeItem) {
                GradeItem gradeItem = (GradeItem) items.get(i);
                if (gradeItem.getHash().equals(newGrade.getHash())) {
                    if (!gradeItem.equals(newGrade)) {
                        // update old grade item and notify ui
                        gradeItem.setName(newGrade.getName());
                        gradeItem.setModifiedName(newGrade.getModifiedName());
                        gradeItem.setGrade(newGrade.getGrade());
                        gradeItem.setModifiedGrade(newGrade.getModifiedGrade());
                        gradeItem.setCreditPoints(newGrade.getCreditPoints());
                        gradeItem.setModifiedCreditPoints(newGrade.getModifiedCreditPoints());
                        gradeItem.setWeight(newGrade.getWeight());
                        gradeItem.setSeen(newGrade.getSeen());
                        gradeItem.setHidden(newGrade.isHidden());
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
        int newGradeIndex = items.size() - 1;
        for(int i = semesterIndex + 1; i < items.size() - 1; i++) {
            if (items.get(i) instanceof GradeItem) {
                GradeItem gradeItem = (GradeItem) items.get(i);
                if (newGrade.getShownName().compareToIgnoreCase(gradeItem.getShownName()) <= 0) {
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
        for(int i = 0; i < items.size() - 1; i++) {
            if (items.get(i) instanceof SemesterItem) {
                SemesterItem semesterItem = (SemesterItem) items.get(i);
                if (semesterNumberMap.get(semesterItem.getSemester()) == semesterNumber) {
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
        int semesterIndex = items.size() - 1; // add to bottom , if no other position will be found

        // find index where the semester should be added (descending by semester number)
        for(int i = 0; i < items.size() - 1; i++) {
            if (items.get(i) instanceof SemesterItem) {
                SemesterItem semesterItem = (SemesterItem) items.get(i);
                if (semesterNumber > semesterNumberMap.get(semesterItem.getSemester())) {
                    semesterIndex = i;
                    break;
                }
            }
        }

        // add new semester
        SemesterItem newSemester = new SemesterItem(simpleWeighting);
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
        for (int i = 0; i < items.size() - 1; i++) {
            GradesAdapterItem item = items.get(i);

            if (item instanceof SemesterItem) {
                SemesterItem semesterItem = (SemesterItem) item;
                semesterItem.setSimpleWeighting(simpleWeighting);
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
            gradeViewHolder.setToggleVisibilityClickListener(new ToggleVisibilityClickListener());
            return gradeViewHolder;
        } else if (viewType == VIEW_TYPE_SUMMARY) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grades_summary, parent, false);
            GradesSummaryViewHolder gradesSummaryViewHolder = new GradesSummaryViewHolder(v);
            gradesSummaryViewHolder.infoBoxMessage.setMovementMethod(LinkMovementMethod.getInstance());
            gradesSummaryViewHolder.dismissInfoBox.setOnClickListener(hideInfoBoxClickListener);
            gradesSummaryViewHolder.btnLogout.setOnClickListener(logoutClickListener);
            return gradesSummaryViewHolder;
        } else if (viewType == VIEW_TYPE_FOOTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grades_footer, parent, false);
            return new FooterViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SemesterViewHolder) {
            SemesterViewHolder viewHolder = (SemesterViewHolder) holder;
            SemesterItem semesterItem = (SemesterItem) items.get(position);

            int semesterNumber = semesterNumberMap.get(semesterItem.getSemester());
            int actualFirstSemesterNumber = semesterNumberMap.get(actualFirstSemester);
            int actualSemesterNumber = (semesterNumber - actualFirstSemesterNumber) + 1;

            viewHolder.tvSemesterNumber.setText("" + actualSemesterNumber + ". Semester");
            viewHolder.tvSemester.setText(semesterItem.getSemester());
            viewHolder.tvAverage.setText("Ø " + String.format("%.2f", semesterItem.getAverage()));
            viewHolder.tvCreditPoints.setText("Σ " + String.format("%.1f", semesterItem.getCreditPoints()) + " CP");

            // hide semester if every grade is hidden
            if (editModeEnabled) {
                viewHolder.hide(false);
            } else {
                viewHolder.hide(semesterItem.getVisibleGradesCount() == 0);
            }
        } else if (holder instanceof GradeViewHolder) {
            GradeViewHolder viewHolder = (GradeViewHolder) holder;
            GradeItem gradeItem = (GradeItem) items.get(position);
            viewHolder.setGradeHash(gradeItem.getHash());
            viewHolder.setHidden(gradeItem.isHidden());
            viewHolder.tvName.setText(gradeItem.getShownName());

            Double grade = gradeItem.getGrade();
            grade = gradeItem.getModifiedGrade() == null ? grade : gradeItem.getModifiedGrade();
            String gradeAsString = grade == null ? "-" : String.format("%.1f", grade);
            viewHolder.tvGrade.setText(gradeAsString);

            Double creditPoints = gradeItem.getCreditPoints();
            creditPoints = gradeItem.getModifiedCreditPoints() == null ? creditPoints : gradeItem.getModifiedCreditPoints();
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

            if (gradeItem.showWeight()) {
                double weight = gradeItem.getWeight() == null ? 1.0 : gradeItem.getWeight();
                viewHolder.tvWeight.setText(String.format("%.1fx", weight));
                viewHolder.tvWeight.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tvWeight.setText("");
                viewHolder.tvWeight.setVisibility(View.GONE);
            }

            viewHolder.modifiedBadge.setVisibility(gradeItem.showModifiedBadge() ? View.VISIBLE : View.GONE);

            if (editModeEnabled) {
                viewHolder.ivToggleVisibility.setVisibility(View.VISIBLE);
                viewHolder.hide(false);
            } else {
                viewHolder.ivToggleVisibility.setVisibility(View.GONE);
                viewHolder.hide(gradeItem.isHidden());
            }

            if (gradeItem.isHidden()) {
                viewHolder.setTransparent(true);
                viewHolder.ivToggleVisibility.setImageResource(R.drawable.ic_visibility_off_white_24dp);
            } else {
                viewHolder.setTransparent(false);
                viewHolder.ivToggleVisibility.setImageResource(R.drawable.ic_visibility_white_24dp);
            }

            viewHolder.enableClickListener(!editModeEnabled);
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
        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            GradesFooterItem footerItem = (GradesFooterItem) items.get(position);
            footerViewHolder.itemView.setVisibility(footerItem.isVisible() ? View.VISIBLE : View.GONE);
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
        } else if (items.get(position) instanceof GradesFooterItem) {
            return VIEW_TYPE_FOOTER;
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

    public void setSemesterNumberMap(Map<String, Integer> semesterNumberMap) {
        if (semesterNumberMap == null) return;
        if (semesterNumberMap.size() < this.semesterNumberMap.size()) return;

        this.semesterNumberMap = semesterNumberMap;
    }

    public void setActualFirstSemester(String actualFirstSemester) {
        if (actualFirstSemester == null) return;

        this.actualFirstSemester = actualFirstSemester;
    }

    public void enableEditMode(boolean enable) {
        editModeEnabled = enable;
        notifyItemRangeChanged(1, items.size());
    }

    public boolean isEditModeEnabled() {
        return editModeEnabled;
    }

    public void restoreVisibility() {
        enableEditMode(false);
        MainServiceHelper mainServiceHelper = new MainServiceHelper(context);

        for (GradesAdapterItem item : items) {
            if (item instanceof GradeItem) {
                GradeItem gradeItem = (GradeItem) item;
                if (gradeItem.isHidden()) {
                    mainServiceHelper.updateGradeEntryVisibility(gradeItem.getHash(), false);
                }
            }
        }
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
     * Click listener to toggle the visibility of a grade entry.
     */
    private static class ToggleVisibilityClickListener implements View.OnClickListener {
        private String gradeHash = "";
        private boolean isHidden;

        private void setGradeHash(String gradeHash) {
            this.gradeHash = gradeHash;
        }

        private void setHidden(boolean hidden) {
            isHidden = hidden;
        }

        @Override
        public void onClick(View v) {
            MainServiceHelper mainServiceHelper = new MainServiceHelper(v.getContext());
            mainServiceHelper.updateGradeEntryVisibility(gradeHash, !isHidden);
        }
    }

    /**
     * View holder for a grade entry.
     */
    public static class GradeViewHolder extends ConcealableViewHolder {
        public RelativeLayout llGradeContainer;
        public TextView tvName;
        public TextView tvGrade;
        public TextView tvCreditPoints;
        public TextView tvGradeSeen;
        public TextView tvWeight;
        public TriangleShapeView modifiedBadge;
        public GoToDetailsClickListener goToDetailsClickListener;
        public ImageView ivToggleVisibility;
        public ToggleVisibilityClickListener toggleVisibilityClickListener;

        public GradeViewHolder(View itemView) {
            super(itemView);

            tvName = (TextView) itemView.findViewById(R.id.tv_grade_name);
            tvGrade = (TextView) itemView.findViewById(R.id.tv_grade);
            tvCreditPoints = (TextView) itemView.findViewById(R.id.tv_credit_points);
            tvGradeSeen = (TextView) itemView.findViewById(R.id.tv_grade_seen);
            llGradeContainer = (RelativeLayout) itemView.findViewById(R.id.grade_container);
            tvWeight = (TextView) itemView.findViewById(R.id.tv_weight);
            modifiedBadge = (TriangleShapeView) itemView.findViewById(R.id.modified_badge);
            ivToggleVisibility = (ImageView) itemView.findViewById(R.id.iv_toggle_visibility);
        }

        public void setTransparent(boolean transparent) {
            for (int i = 0; i < llGradeContainer.getChildCount(); i++) {
                llGradeContainer.getChildAt(i).setAlpha(transparent ? 0.5f : 1.0f);
            }
        }

        public void setGoToDetailsClickListener(GoToDetailsClickListener clickListener) {
            this.goToDetailsClickListener = clickListener;
            llGradeContainer.setOnClickListener(goToDetailsClickListener);
        }

        public void setToggleVisibilityClickListener(ToggleVisibilityClickListener clickListener) {
            this.toggleVisibilityClickListener = clickListener;
            ivToggleVisibility.setOnClickListener(toggleVisibilityClickListener);
        }

        public void enableClickListener(boolean enable) {
            llGradeContainer.setOnClickListener(enable ? goToDetailsClickListener : null);
        }

        public void setGradeHash(String gradeHash) {
            goToDetailsClickListener.setGradeHash(gradeHash);
            toggleVisibilityClickListener.setGradeHash(gradeHash);
        }

        public void setHidden(boolean hidden) {
            toggleVisibilityClickListener.setHidden(hidden);
        }
    }

    /**
     * View holder for an semester section header.
     */
    public static class SemesterViewHolder extends ConcealableViewHolder {
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
     * View holder for the footer.
     */
    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
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

    /**
     * Abstract view holder to add the ability to hide it.
     * If shown, the height is set to RecyclerView.LayoutParams.WRAP_CONTENT,
     * whereas the width is untouched.
     */
    public static abstract class ConcealableViewHolder extends RecyclerView.ViewHolder {

        public ConcealableViewHolder(View itemView) {
            super(itemView);
        }

        public void hide(boolean hide) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            params.height = hide ? 0 : RecyclerView.LayoutParams.WRAP_CONTENT;
            itemView.setLayoutParams(params);
        }
    }
}
