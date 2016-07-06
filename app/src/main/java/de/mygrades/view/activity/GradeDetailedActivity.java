package de.mygrades.view.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    // intent extra data
    public static final String EXTRA_GRADE_HASH = "grade_hash";
    public static final String EXTRA_ADD_NEW_GRADE_ENTRY = "add_new_grade_entry";

    // instance state
    private static final String IS_OVERVIEW_POSSIBLE_STATE = "is_overview_possible_state";
    private static final String GRADE_HASH_STATE = "grade_hash";
    private static final String EDIT_MODE_ENABLED_STATE = "edit_mode_enabled";
    private static final String ADD_NEW_GRADE_ENTRY = "add_new_grade_entry";
    private static final String EDIT_HELPER_STATE = "edit_helper_state";

    // views
    private LinearLayout llRootView; // used to show snackbar
    private PtrFrameLayout ptrFrame;
    private PtrHeader ptrHeader;

    // views for overview
    private LinearLayout llOverviewWrapper;
    private TextView tvOverviewParticipants;
    private TextView tvOverviewPassed;
    private TextView tvOverviewAverage;
    private TextView tvOverviewNotPossible;
    private TextView tvCustomGradeEntry;

    // bar chart
    private Overview overview;
    private BarChart barChart;
    private static final int COLOR_GRAY = Color.rgb(233, 233, 233); // light gray
    private static final int COLOR_HIGHLIGHT = Color.rgb(139, 195, 74); // primary color green

    private String gradeHash;
    private GradeEntry gradeEntry;
    private MainServiceHelper mainServiceHelper;
    private GradeDetailedActivityEditHelper editHelper;
    private boolean isOverviewPossible;
    private boolean receivedGradeEntryEvent;
    private boolean editModeEnabled;
    private boolean addNewGradeEntry;
    private Bundle editHelperState;

    // snackbar listener
    private View.OnClickListener tryAgainListener;
    private View.OnClickListener goToFaqListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_detailed);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.toolbar_details);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mainServiceHelper = new MainServiceHelper(this);
        editHelper = new GradeDetailedActivityEditHelper(this);

        // get extra data
        Bundle extras = getIntent().getExtras();
        gradeHash = extras.getString(EXTRA_GRADE_HASH, "");
        addNewGradeEntry = extras.getBoolean(EXTRA_ADD_NEW_GRADE_ENTRY, false);

        initListener();
        initViews();

        // register event bus
        EventBus.getDefault().register(this);

        // start intent to get data for Grade Detail page
        mainServiceHelper.getGradeDetails(gradeHash);
    }

    /**
     * Initialize all needed views.
     */
    private void initViews() {
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
        tvCustomGradeEntry = (TextView) findViewById(R.id.tv_custom_grade_entry);

        initPullToRefresh();
    }

    private void initListener() {
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
     * @param gradeEntryEvent GradeEntryEvent
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(GradeEntryEvent gradeEntryEvent) {
        gradeEntry = gradeEntryEvent.getGradeEntry();
        gradeHash = gradeEntry.getHash();

        editHelper.setGradeEntry(gradeEntry);
        editHelper.setSemesterToNumberMap(gradeEntryEvent.getSemesterToSemesterNumberMap());
        editHelper.init();
        editHelper.updateValues();
        if (editModeEnabled) {
            editHelper.restoreInstanceState(editHelperState);
        }

        if (addNewGradeEntry) {
            editHelper.enableEditMode(true);
            editModeEnabled = true;
        } else {
            editHelper.enableEditMode(editModeEnabled);
        }

        if (gradeEntry.isCustomGradeEntry()) {
            tvCustomGradeEntry.setVisibility(View.VISIBLE);
        }

        receivedGradeEntryEvent = true;
        invalidateOptionsMenu();
    }

    /**
     * Receive an event when the Overview is retrieved.
     * Set TextViews to values from Overview.
     * @param overviewEvent OverviewEvent
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(OverviewEvent overviewEvent) {
        if (overview != null)
            return;

        overview = overviewEvent.getOverview();

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
     * @param overviewPossibleEvent OverviewPossibleEvent
     */
    @SuppressWarnings("unused")
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
                if (overviewPossibleEvent.isOverviewPossibleForUniversity() && !gradeEntry.isCustomGradeEntry()) {
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public void onEventMainThread(ErrorEvent errorEvent) {
        if (ptrFrame != null && ptrHeader != null) {
            ptrHeader.setIsError(true);
            ptrFrame.refreshComplete();
        }

        UIHelper.displayErrorMessage(llRootView, errorEvent, tryAgainListener, goToFaqListener);
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
        outState.putBoolean(EDIT_MODE_ENABLED_STATE, editModeEnabled);
        outState.putBoolean(ADD_NEW_GRADE_ENTRY, addNewGradeEntry);
        outState.putBundle(EDIT_HELPER_STATE, editHelper.getInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        isOverviewPossible = savedInstanceState.getBoolean(IS_OVERVIEW_POSSIBLE_STATE);
        gradeHash = savedInstanceState.getString(GRADE_HASH_STATE, "");
        ptrHeader.restoreInstanceState(savedInstanceState, ptrFrame);
        editModeEnabled = savedInstanceState.getBoolean(EDIT_MODE_ENABLED_STATE);
        addNewGradeEntry = savedInstanceState.getBoolean(ADD_NEW_GRADE_ENTRY);
        editHelperState = savedInstanceState.getBundle(EDIT_HELPER_STATE);
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

        MenuItem editItem = menu.findItem(R.id.grade_detail_edit);
        MenuItem saveItem = menu.findItem(R.id.grade_detail_save);
        MenuItem restoreItem = menu.findItem(R.id.grade_detail_restore);
        MenuItem deleteItem = menu.findItem(R.id.grade_detail_delete);
        editItem.setVisible(receivedGradeEntryEvent && !editHelper.isEditModeEnabled());
        saveItem.setVisible(receivedGradeEntryEvent && editHelper.isEditModeEnabled());
        restoreItem.setVisible(receivedGradeEntryEvent && editHelper.isEditModeEnabled() && !editHelper.isCustomGradeEntry());
        deleteItem.setVisible(receivedGradeEntryEvent && editHelper.isEditModeEnabled() && editHelper.isCustomGradeEntry());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.grade_detail_edit:
                editHelper.enableEditMode(true);
                editModeEnabled = true;
                invalidateOptionsMenu();
                return true;
            case R.id.grade_detail_save:
                editHelper.enableEditMode(false);
                editHelper.saveEdits();
                addNewGradeEntry = false;
                editModeEnabled = false;
                invalidateOptionsMenu();
                return true;
            case R.id.grade_detail_delete:
                showDeleteDialog();
                return true;
            case R.id.grade_detail_restore:
                showRestoreDialog();
                return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if (addNewGradeEntry) {
            showDiscardDialog();
            return;
        }

        if (editModeEnabled) {
            editHelper.enableEditMode(false);
            editModeEnabled = false;
            invalidateOptionsMenu();
            mainServiceHelper.getGradeDetails(gradeHash);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Show dialog to ask the user whether he wants to restore the original GradeEntry.
     */
    private void showRestoreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_restore_grade_entry);
        builder.setTitle(getString(R.string.dialog_restore_grade_entry_title));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editHelper.restore();
                editModeEnabled = false;
                invalidateOptionsMenu();
            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Show dialog to ask the user whether he wants to delete this GradeEntry.
     */
    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_delete_grade_entry);
        builder.setTitle(getString(R.string.dialog_delete_grade_entry_title));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                editHelper.delete();
                editModeEnabled = false;
                finish();
            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Show dialog to ask the user whether he wants to discard this new GradeEntry.
     */
    private void showDiscardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_discard_grade_entry);
        builder.setTitle(getString(R.string.dialog_discard_grade_entry_title));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
