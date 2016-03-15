package de.mygrades.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.database.dao.GradeEntry;
import de.mygrades.database.dao.Overview;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.ErrorEvent;
import de.mygrades.main.events.GradeEntryEvent;
import de.mygrades.main.events.OverviewEvent;
import de.mygrades.main.events.OverviewPossibleEvent;
import de.mygrades.main.events.ScrapeProgressEvent;
import de.mygrades.view.PtrHeader;
import de.mygrades.view.UIHelper;
import de.mygrades.view.adapter.dataprovider.FaqDataProvider;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Activity to show detailed information for a specific grade entry.
 * If available for university and grade entry a diagram for overview of grades is shown.
 */
public class GradeDetailedActivity extends AppCompatActivity {
    private static final String TAG = GradeDetailedActivity.class.getSimpleName();

    public static final String EXTRA_GRADE_HASH = "grade_hash";

    private String gradeHash;
    private GradeEntry gradeEntry;
    private MainServiceHelper mainServiceHelper;

    private boolean editModeEnabled;

    private PtrFrameLayout ptrFrame;
    private PtrHeader ptrHeader;

    // Views
    private TextView tvGradeDetailName;
    private EditText etGradeDetailExamId;
    private TextView tvGradeDetailSemester;
    private EditText etGradeDetailState;
    private EditText etGradeDetailCreditPoints;
    private EditText etGradeDetailGrade;
    private EditText etGradeDetailAnnotation;
    private TextView tvGradeDetailAttempt;
    private EditText etGradeDetailExamDate;
    private EditText etGradeDetailTester;
    private EditText etGradeDetailWeight;

    private LinearLayout llModifiedHint;

    private LinearLayout llOverviewWrapper;
    private TextView tvOverviewParticipants;
    private TextView tvOverviewPassed;
    private TextView tvOverviewAverage;

    private TextView tvOverviewNotPossible;

    private LinearLayout llRootView; // used to show snackbar

    private BarChart barChart;
    private static final int COLOR_GRAY = Color.rgb(233, 233, 233); // light gray
    private static final int COLOR_HIGHLIGHT = Color.rgb(139, 195, 74); // primary color green

    private static final String IS_OVERVIEW_POSSIBLE_STATE = "is_overview_possible_state";
    private static final String GRADE_HASH_STATE = "grade_hash";

    private boolean isOverviewPossible;

    // snackbar buttons
    private View.OnClickListener tryAgainListener;
    private View.OnClickListener goToFaqListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_detailed);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.toolbar_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mainServiceHelper = new MainServiceHelper(this);
        editModeEnabled = false;

        // get extra data
        Bundle extras = getIntent().getExtras();
        gradeHash = extras.getString(EXTRA_GRADE_HASH, "");

        // click listener to go to the FAQs and show the general-error question immediately
        goToFaqListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GradeDetailedActivity.this, MainActivity.class);
                intent.putExtra(FragmentFaq.ARGUMENT_GO_TO_QUESTION, FaqDataProvider.GO_TO_GENERAL_ERROR);
                GradeDetailedActivity.this.startActivity(intent);
            }
        };

        // click listener to restart the scraping process
        tryAgainListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ptrFrame.autoRefresh();
                mainServiceHelper.scrapeForOverview(gradeHash);
            }
        };

        initViews();
        enableEditMode(false);

        // restore instance state if necessary
        if (savedInstanceState != null) {
            isOverviewPossible = savedInstanceState.getBoolean(IS_OVERVIEW_POSSIBLE_STATE);
            gradeHash = savedInstanceState.getString(GRADE_HASH_STATE, "");
            ptrHeader.restoreInstanceState(savedInstanceState, ptrFrame);
        }

        // register event bus
        EventBus.getDefault().register(this);

        // start intent to get data for Grade Detail page
        mainServiceHelper.getGradeDetails(gradeHash);
    }

    /**
     * Initialize all needed views.
     */
    private void initViews() {
        // get views for grade
        tvGradeDetailName = (TextView) findViewById(R.id.tv_grade_detail_name);
        tvGradeDetailSemester = (TextView) findViewById(R.id.tv_grade_detail_semester);
        etGradeDetailCreditPoints = (EditText) findViewById(R.id.et_grade_detail_credit_points);
        etGradeDetailGrade = (EditText) findViewById(R.id.et_grade_detail_grade);
        etGradeDetailAnnotation = (EditText) findViewById(R.id.et_grade_detail_annotation);
        tvGradeDetailAttempt = (TextView) findViewById(R.id.tv_grade_detail_attempt);
        etGradeDetailExamDate = (EditText) findViewById(R.id.et_grade_detail_exam_date);

        // editable views
        etGradeDetailExamId = (EditText) findViewById(R.id.et_grade_detail_exam_id);
        etGradeDetailTester = (EditText) findViewById(R.id.et_grade_detail_tester);
        etGradeDetailState = (EditText) findViewById(R.id.et_grade_detail_state);
        etGradeDetailWeight = (EditText) findViewById(R.id.et_grade_detail_weight);

        llModifiedHint = (LinearLayout) findViewById(R.id.ll_modified_hint);

        // get views for overview
        llOverviewWrapper = (LinearLayout) findViewById(R.id.overview_wrapper);
        tvOverviewParticipants = (TextView) findViewById(R.id.tv_overview_participants);
        tvOverviewPassed = (TextView) findViewById(R.id.tv_overview_passed);
        tvOverviewAverage = (TextView) findViewById(R.id.tv_overview_average);
        barChart = (BarChart) findViewById(R.id.bar_chart);
        tvOverviewNotPossible = (TextView) findViewById(R.id.tv_overview_not_possible);
        tvOverviewNotPossible.setText(Html.fromHtml(getString(R.string.overview_not_possible, FaqDataProvider.GO_TO_WHY_NO_GRADING)));
        tvOverviewNotPossible.setMovementMethod(LinkMovementMethod.getInstance());
        llRootView = (LinearLayout) findViewById(R.id.ll_root_view);

        initPullToRefresh();
    }

    /**
     * Initialize the pull to refresh layout.
     */
    private void initPullToRefresh() {
        ptrFrame = (PtrFrameLayout) findViewById(R.id.pull_to_refresh);
        ptrHeader = new PtrHeader(this, getString(R.string.ptr_header_refreshing_overview));
        ptrFrame.addPtrUIHandler(ptrHeader);
        ptrFrame.setHeaderView(ptrHeader);

        ptrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                if (!ptrFrame.isAutoRefresh()) {
                    mainServiceHelper.scrapeForOverview(gradeHash);
                }
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });
    }

    /**
     * Shows the bar chart with data taken from an OverviewEvent.
     *
     * @param overviewEvent - overview event
     */
    private void showBarChart(OverviewEvent overviewEvent) {
        // general layout settings
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setDescription("");
        barChart.setDrawBorders(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setEnabled(false);

        // set x-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(0);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.text87));
        xAxis.setLabelsToSkip(0);

        // hide left y-axis
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setDrawAxisLine(false);
        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisLeft().setGridColor(ContextCompat.getColor(this, R.color.divider));
        barChart.getAxisLeft().setLabelCount(6, true);

        // hide right y-axis
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisRight().setDrawAxisLine(false);
        barChart.getAxisRight().setDrawLabels(false);

        Overview overview = overviewEvent.getOverview();

        // y-values
        ArrayList<BarEntry> yValues = new ArrayList<>();
        yValues.add(new BarEntry(overview.getSection1(), 0));
        yValues.add(new BarEntry(overview.getSection2(), 1));
        yValues.add(new BarEntry(overview.getSection3(), 2));
        yValues.add(new BarEntry(overview.getSection4(), 3));
        yValues.add(new BarEntry(overview.getSection5(), 4));

        // x-values
        ArrayList<String> xValues = new ArrayList<>();
        xValues.add("1,0 - 1,3");
        xValues.add("1,7 - 2,3");
        xValues.add("2,7 - 3,3");
        xValues.add("3,7 - 4,0");
        xValues.add("4,3 - 5,0");

        // set colors
        int[] BAR_CHART_COLORS = {COLOR_GRAY, COLOR_GRAY, COLOR_GRAY, COLOR_GRAY, COLOR_GRAY};
        if (overview.getUserSection() != null && overview.getUserSection() > 0) {
            BAR_CHART_COLORS[overview.getUserSection() - 1] = COLOR_HIGHLIGHT;
        }

        BarDataSet dataSet = new BarDataSet(yValues, "");
        dataSet.setValueTextSize(12);
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.text87));
        dataSet.setColors(BAR_CHART_COLORS);
        dataSet.setDrawValues(true);
        dataSet.setBarSpacePercent(35);

        BarData barData = new BarData(xValues, dataSet);
        barData.setValueFormatter(new MyValueFormatter());
        barData.setHighlightEnabled(false);
        barChart.setData(barData);

        // add a nice and smooth animation
        barChart.animateY(1500);
        barChart.invalidate();
    }

    /**
     * Receive an event when the GradeEntry is retrieved from the DB.
     * Set TextViews to values from Grade and evaluate which one is shown.
     * @param gradeEntryEvent
     */
    public void onEventMainThread(GradeEntryEvent gradeEntryEvent) {
        gradeEntry = gradeEntryEvent.getGradeEntry();
        updateValues();
    }

    /**
     * Receive an event when the Overview is retrieved.
     * Set TextViews to values from Overview.
     * @param overviewEvent
     */
    public void onEventMainThread(OverviewEvent overviewEvent) {
        Log.d(TAG, overviewEvent.getOverview().toString());
        Overview overview = overviewEvent.getOverview();

        // only set overview if it belongs to current gradeEntry
        if (gradeEntry != null && overview.getGradeEntryHash().equals(gradeHash)) {
            // if it's a scraping result
            if (overviewEvent.isScrapingResult() && ptrFrame != null && ptrHeader != null) {
                ptrFrame.refreshComplete();
            }

            if (overview.getParticipants() != null) {
                llOverviewWrapper.setVisibility(View.VISIBLE);
                tvOverviewParticipants.setText(String.valueOf(overview.getParticipants()));

                // calculated passed participants
                int participants = overview.getParticipants();
                int section5 = overview.getSection5();
                if (participants > 0) {
                    int passedParticipants = participants - section5;
                    int passedParticipantsPercent = (int) Math.round(passedParticipants / (double) participants * 100);
                    tvOverviewPassed.setText(String.format("%d (%d%%)", passedParticipants, passedParticipantsPercent));
                }

                writeDoubleToTextView(tvOverviewAverage, overview.getAverage());

                showBarChart(overviewEvent);
            }
        }
    }

    /**
     * Receive an event when its determined whether it is possible to receive an overview.
     * Event is only sent if there is no overview for this grade entry in DB.
     * @param overviewPossibleEvent
     */
    public void onEventMainThread(OverviewPossibleEvent overviewPossibleEvent) {
        if (ptrHeader != null && !ptrHeader.isScraping()) {
            isOverviewPossible = overviewPossibleEvent.isOverviewPossible();

            if (isOverviewPossible) {
                ptrFrame.autoRefresh();
                mainServiceHelper.scrapeForOverview(gradeHash);
            } else {
                ptrFrame.setPtrHandler(new PtrHandler() {
                    @Override
                    public void onRefreshBegin(PtrFrameLayout frame) {

                    }

                    @Override
                    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                        return false;
                    }
                });
                // only show TextView if its possible in general for university to show a overview
                if (overviewPossibleEvent.isOverviewPossibleForUniversity()) {
                    tvOverviewNotPossible.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * Receive an event about the scraping progress and set the ProgressWheel accordingly.
     *
     * @param scrapeProgressEvent ScrapeProgressEvent
     */
    public void onEventMainThread(ScrapeProgressEvent scrapeProgressEvent) {
        if (ptrHeader != null && scrapeProgressEvent.isScrapeForOverview()
                && gradeHash != null && gradeHash.equals(scrapeProgressEvent.getGradeHash())) {
            ptrHeader.increaseProgress(scrapeProgressEvent.getCurrentStep(), scrapeProgressEvent.getStepCount());
        }
    }

    /**
     * Receive error events.
     *
     * @param errorEvent ErrorEvent
     */
    public void onEventMainThread(ErrorEvent errorEvent) {
        if (ptrFrame != null && ptrHeader != null) {
            ptrHeader.setIsError(true);
            ptrFrame.refreshComplete();
        }

        UIHelper.displayErrorMessage(llRootView, errorEvent, tryAgainListener, goToFaqListener);
    }

    private boolean setTextView(TextView textView, String value, String modifiedValue, int resIdModifiedBadge) {
        String v = value;
        boolean modified = false;
        if (modifiedValue != null) {
            v = modifiedValue;
            modified = true;
        }

        ViewGroup parent = (ViewGroup)textView.getParent();
        if (v != null) {
            textView.setText(v);
            parent.setVisibility(View.VISIBLE);
        } else {
            parent.setVisibility(View.GONE);
        }

        View badge = parent.findViewById(resIdModifiedBadge);
        if (badge != null) {
            badge.setVisibility(modified ? View.VISIBLE : View.GONE);
        }

        return modified;
    }

    private boolean setTextView(TextView textView, Double value, Double modifiedValue, boolean forcedVisible, int resIdModifiedBadge) {
        Double v = value;
        boolean modified = false;
        if (modifiedValue != null) {
            v = modifiedValue;
            modified = true;
        }

        ViewGroup parent = (ViewGroup)textView.getParent();
        if (v != null || forcedVisible) {
            writeDoubleToTextView(textView, v);
            parent.setVisibility(View.VISIBLE);
        }

        View badge = parent.findViewById(resIdModifiedBadge);
        if (badge != null) {
            badge.setVisibility(modified ? View.VISIBLE : View.GONE);
        }

        return modified;
    }

    private boolean setWeightValue(EditText editText, Double weight, int resIdModifiedBadge) {
        double value = weight == null ? 1 : weight;
        boolean modified = value != 1;

        ViewGroup parent = (ViewGroup)editText.getParent();
        editText.setText("" + value);
        parent.setVisibility(View.VISIBLE);

        View badge = parent.findViewById(resIdModifiedBadge);
        if (badge != null) {
            badge.setVisibility(modified ? View.VISIBLE : View.GONE);
        }

        return modified;
    }

    private void writeDoubleToTextView(TextView textView, Double value) {
        String valueAsString = value == null ? "-" : String.format("%.1f", value);
        textView.setText(valueAsString);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregister EventBus
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ptrHeader.saveInstanceState(outState);
        outState.putBoolean(IS_OVERVIEW_POSSIBLE_STATE, isOverviewPossible);
        outState.putString(GRADE_HASH_STATE, gradeHash);
    }

    private class MyValueFormatter implements ValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("#"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return mFormat.format(value);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grade_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.grade_detail_edit:
                enableEditMode(true);
                return true;
            case R.id.grade_detail_save:
                saveEdits();
                return true;
        }

        return false;
    }

    private void enableEditMode(boolean enable) {
        editModeEnabled = enable;

        // enable all edit texts
        etGradeDetailExamId.setEnabled(editModeEnabled);
        etGradeDetailWeight.setEnabled(editModeEnabled);
        etGradeDetailTester.setEnabled(editModeEnabled);
        etGradeDetailState.setEnabled(editModeEnabled);
        etGradeDetailAnnotation.setEnabled(editModeEnabled);
        etGradeDetailExamDate.setEnabled(editModeEnabled);
        etGradeDetailGrade.setEnabled(editModeEnabled);
        etGradeDetailCreditPoints.setEnabled(editModeEnabled);

        // show all edit texts
        if (editModeEnabled) {
            ((View) tvGradeDetailName.getParent()).setVisibility(View.VISIBLE);
            ((View) etGradeDetailExamId.getParent()).setVisibility(View.VISIBLE);
            ((View) tvGradeDetailSemester.getParent()).setVisibility(View.VISIBLE);
            ((View) etGradeDetailState.getParent()).setVisibility(View.VISIBLE);
            ((View) etGradeDetailCreditPoints.getParent()).setVisibility(View.VISIBLE);
            ((View) etGradeDetailGrade.getParent()).setVisibility(View.VISIBLE);
            ((View) etGradeDetailAnnotation.getParent()).setVisibility(View.VISIBLE);
            ((View) tvGradeDetailAttempt.getParent()).setVisibility(View.VISIBLE);
            ((View) etGradeDetailExamDate.getParent()).setVisibility(View.VISIBLE);
            ((View) etGradeDetailTester.getParent()).setVisibility(View.VISIBLE);
            ((View) etGradeDetailWeight.getParent()).setVisibility(View.VISIBLE);
        }
    }

    private void saveEdits() {
        // disable edit mode
        enableEditMode(false);

        boolean modified = false;

        // TODO: also check if gradeEntry.modified* differs from new input
        // to avoid unnecessary update?

        // check exam id
        String examId = gradeEntry.getExamId();
        String modifiedExamId = etGradeDetailExamId.getText().toString();
        modifiedExamId = modifiedExamId.length() == 0 ? null : modifiedExamId;
        if (examId == null || !examId.equals(modifiedExamId)) {
            gradeEntry.setModifiedExamId(modifiedExamId);
            modified = true;
        } else if (examId.equals(modifiedExamId)) {
            // remove modified exam id if the input equals the original value
            gradeEntry.setModifiedExamId(null);
            modified = true;
        }

        // check weight // TODO: replace with spinner
        Double weight = gradeEntry.getWeight();
        String weightInput = etGradeDetailWeight.getText().toString();
        Double modifiedWeight = weightInput.length() == 0 ? 1 : Double.parseDouble(weightInput);
        if (weight == null || !weight.equals(modifiedWeight)) {
            gradeEntry.setWeight(modifiedWeight);
            modified = true;
        }

        // check tester
        String tester = gradeEntry.getTester();
        String modifiedTester = etGradeDetailTester.getText().toString();
        modifiedTester = modifiedTester.length() == 0 ? null : modifiedTester;
        if (tester == null || !tester.equals(modifiedTester)) {
            gradeEntry.setModifiedTester(modifiedTester);
            modified = true;
        } else if (tester.equals(modifiedTester)) {
            gradeEntry.setModifiedTester(null);
            modified = true;
        }

        // check state
        String state = gradeEntry.getState();
        String modifiedState = etGradeDetailState.getText().toString();
        modifiedState = modifiedState.length() == 0 ? null : modifiedState;
        if (state == null || !state.equals(modifiedState)) {
            gradeEntry.setModifiedState(modifiedState);
            modified = true;
        } else if (state.equals(modifiedState)) {
            gradeEntry.setModifiedState(null);
            modified = true;
        }

        // check annotation
        String annotation = gradeEntry.getAnnotation();
        String modifiedAnnotation = etGradeDetailAnnotation.getText().toString();
        modifiedAnnotation = modifiedAnnotation.length() == 0 ? null : modifiedAnnotation;
        if (annotation == null || !annotation.equals(modifiedAnnotation)) {
            gradeEntry.setModifiedAnnotation(modifiedAnnotation);
            modified = true;
        } else if (annotation.equals(modifiedAnnotation)) {
            gradeEntry.setModifiedAnnotation(null);
            modified = true;
        }

        // check exam date
        String examDate = gradeEntry.getExamDate();
        String modifiedExamDate = etGradeDetailExamDate.getText().toString();
        modifiedExamDate = modifiedExamDate.length() == 0 ? null : modifiedExamDate;
        if (examDate == null || !examDate.equals(modifiedExamDate)) {
            gradeEntry.setModifiedExamDate(modifiedExamDate);
            modified = true;
        } else if (examDate.equals(modifiedExamDate)) {
            gradeEntry.setModifiedExamDate(null);
            modified = true;
        }

        // check grade // TODO: do not show error if input is "-"
        Double grade = gradeEntry.getGrade();
        String modifiedGradeAsString = etGradeDetailGrade.getText().toString();
        modifiedGradeAsString = modifiedGradeAsString.length() == 0 ? null : modifiedGradeAsString;
        if (modifiedGradeAsString == null) {
            gradeEntry.setModifiedGrade(null);
            modified = true;
        } else {
            Double modifiedGrade = stringToDouble(modifiedGradeAsString);
            if (modifiedGrade == null || modifiedGrade < 0 || modifiedGrade > 5) {
                etGradeDetailGrade.setError("Note muss zwischen 0 und 5 liegen."); // TODO: string resource
            } else if (grade == null || !grade.equals(modifiedGrade)) {
                gradeEntry.setModifiedGrade(modifiedGrade);
                modified = true;
            } else if (grade.equals(modifiedGrade)) {
                gradeEntry.setModifiedGrade(null);
                modified = true;
            }
        }

        // check credit points
        Double creditPoints = gradeEntry.getCreditPoints();
        String modifiedCreditPointsAsString = etGradeDetailCreditPoints.getText().toString();
        modifiedCreditPointsAsString = modifiedCreditPointsAsString.length() == 0 ? null : modifiedCreditPointsAsString;
        if (modifiedCreditPointsAsString == null) {
            gradeEntry.setModifiedCreditPoints(null);
            modified = true;
        } else {
            Double modifiedCreditPoints = stringToDouble(modifiedCreditPointsAsString);
            if (modifiedCreditPoints == null || modifiedCreditPoints < 0) {
                etGradeDetailCreditPoints.setError("Credit Points dürfen nicht negativ sein."); // TODO: string resource
            } else if (creditPoints == null || !creditPoints.equals(modifiedCreditPoints)) {
                gradeEntry.setModifiedCreditPoints(modifiedCreditPoints);
                modified = true;
            } else if (creditPoints.equals(modifiedCreditPoints)) {
                gradeEntry.setModifiedCreditPoints(null);
                modified = true;
            }
        }

        if (modified) {
            // update grade in database
            mainServiceHelper.updateGradeEntry(gradeEntry);
        }

        // update ui anyway to hide empty properties
        updateValues();
    }

    private void updateValues() {
        int modifiedCounter = 0;

        tvGradeDetailName.setText(gradeEntry.getName());
        modifiedCounter += setTextView(etGradeDetailExamId, gradeEntry.getExamId(), gradeEntry.getModifiedExamId(), R.id.modified_badge_exam_id) ? 1 : 0;
        modifiedCounter += setTextView(tvGradeDetailSemester, gradeEntry.getSemester(), null, -1) ? 1 : 0;
        modifiedCounter += setTextView(etGradeDetailState, gradeEntry.getState(), gradeEntry.getModifiedState(), R.id.modified_badge_state) ? 1 : 0;
        modifiedCounter += setTextView(etGradeDetailCreditPoints, gradeEntry.getCreditPoints(), gradeEntry.getModifiedCreditPoints(), true, R.id.modified_badge_credit_points) ? 1 : 0;
        modifiedCounter += setTextView(etGradeDetailGrade, gradeEntry.getGrade(), gradeEntry.getModifiedGrade(), true, R.id.modified_badge_grade) ? 1 : 0;
        modifiedCounter += setTextView(etGradeDetailAnnotation, gradeEntry.getAnnotation(), gradeEntry.getModifiedAnnotation(), R.id.modified_badge_annotation) ? 1 : 0;
        modifiedCounter += setTextView(tvGradeDetailAttempt, gradeEntry.getAttempt(), gradeEntry.getModifiedAttempt(), R.id.modified_badge_attempt) ? 1 : 0;
        modifiedCounter += setTextView(etGradeDetailExamDate, gradeEntry.getExamDate(), gradeEntry.getModifiedExamDate(), R.id.modified_badge_exam_date) ? 1 : 0;
        modifiedCounter += setTextView(etGradeDetailTester, gradeEntry.getTester(), gradeEntry.getModifiedTester(), R.id.modified_badge_tester) ? 1 : 0;
        modifiedCounter += setWeightValue(etGradeDetailWeight, gradeEntry.getWeight(), R.id.modified_badge_weight) ? 1 : 0;

        // show or hide hint
        llModifiedHint.setVisibility(modifiedCounter > 0 ? View.VISIBLE : View.GONE);
    }

    /**
     * Converts a string to double.
     * If the string is empty or the parsing fails, null will be returned.
     *
     * @param s - string to convert
     * @return Double or null
     */
    private Double stringToDouble(String s) {
        if (s.length() == 0) {
            return null;
        }

        s = s.replace(',', '.');

        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }
}
