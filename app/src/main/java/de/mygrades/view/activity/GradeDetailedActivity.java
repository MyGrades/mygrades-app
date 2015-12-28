package de.mygrades.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
import com.pnikosis.materialishprogress.ProgressWheel;

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

    private PtrFrameLayout ptrFrame;
    private PtrHeader ptrHeader;

    // Views
    private TextView tvGradeDetailName;
    private TextView tvGradeDetailExamId;
    private TextView tvGradeDetailSemester;
    private TextView tvGradeDetailState;
    private TextView tvGradeDetailCreditPoints;
    private TextView tvGradeDetailGrade;
    private TextView tvGradeDetailAnnotation;
    private TextView tvGradeDetailAttempt;
    private TextView tvGradeDetailExamDate;
    private TextView tvGradeDetailTester;

    private LinearLayout llOverviewWrapper;
    private TextView tvOverviewParticipants;
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
        tvGradeDetailExamId = (TextView) findViewById(R.id.tv_grade_detail_exam_id);
        tvGradeDetailSemester = (TextView) findViewById(R.id.tv_grade_detail_semester);
        tvGradeDetailState = (TextView) findViewById(R.id.tv_grade_detail_state);
        tvGradeDetailCreditPoints = (TextView) findViewById(R.id.tv_grade_detail_credit_points);
        tvGradeDetailGrade = (TextView) findViewById(R.id.tv_grade_detail_grade);
        tvGradeDetailAnnotation = (TextView) findViewById(R.id.tv_grade_detail_annotation);
        tvGradeDetailAttempt = (TextView) findViewById(R.id.tv_grade_detail_attempt);
        tvGradeDetailExamDate = (TextView) findViewById(R.id.tv_grade_detail_exam_date);
        tvGradeDetailTester = (TextView) findViewById(R.id.tv_grade_detail_tester);

        // get views for overview
        llOverviewWrapper = (LinearLayout) findViewById(R.id.overview_wrapper);
        tvOverviewParticipants = (TextView) findViewById(R.id.tv_overview_participants);
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

        // show left y-axis
        barChart.getAxisLeft().setDrawAxisLine(false);
        barChart.getAxisLeft().setGridColor(ContextCompat.getColor(this, R.color.divider));
        barChart.getAxisLeft().setTextColor(ContextCompat.getColor(this, R.color.text87));
        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setDrawLabels(true);
        barChart.getAxisLeft().setLabelCount(6, true);

        // hide right y-axis
        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisRight().setDrawAxisLine(false);
        barChart.getAxisRight().setDrawLabels(false);

        Overview overview = overviewEvent.getOverview();

        // set max y-value
        int max = overview.getMaxSection() + 1;
        max = (int) (Math.ceil(max/10f) * 10); // round to next factor of 10
        barChart.getAxisLeft().setAxisMaxValue(max);

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

        tvGradeDetailName.setText(gradeEntry.getName());
        setTextView(tvGradeDetailExamId, gradeEntry.getExamId());
        setTextView(tvGradeDetailSemester, gradeEntry.getSemester());
        setTextView(tvGradeDetailState, gradeEntry.getState());
        setTextView(tvGradeDetailCreditPoints, gradeEntry.getCreditPoints(), true);
        setTextView(tvGradeDetailGrade, gradeEntry.getGrade(), true);
        setTextView(tvGradeDetailAnnotation, gradeEntry.getAnnotation());
        setTextView(tvGradeDetailAttempt, gradeEntry.getAttempt());
        setTextView(tvGradeDetailExamDate, gradeEntry.getExamDate());
        setTextView(tvGradeDetailTester, gradeEntry.getTester());
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

            llOverviewWrapper.setVisibility(View.VISIBLE);
            tvOverviewParticipants.setText(String.valueOf(overview.getParticipants()));
            writeDoubleToTextView(tvOverviewAverage, overview.getAverage());

            showBarChart(overviewEvent);
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

    private void setTextView(TextView textView, String value) {
        if (value != null) {
            textView.setText(value);
            ((View)textView.getParent()).setVisibility(View.VISIBLE);
        }
    }

    private void setTextView(TextView textView, Double value, boolean forcedVisible) {
        if (value != null || forcedVisible) {
            writeDoubleToTextView(textView, value);
            ((View)textView.getParent()).setVisibility(View.VISIBLE);
        }
    }

    private void setTextView(TextView textView, Double value) {
        setTextView(textView, value, false);
    }

    private void writeDoubleToTextView(TextView textView, Double value) {
        String valueAsString = value == null ? "-" : String.format("%.1f", value);
        textView.setText(valueAsString);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
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
}
