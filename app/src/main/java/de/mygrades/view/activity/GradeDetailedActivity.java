package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

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

/**
 * Created by jonastheis on 03.10.15.
 */
public class GradeDetailedActivity extends AppCompatActivity {
    private static final String TAG = GradeDetailedActivity.class.getSimpleName();

    public static final String EXTRA_GRADE_HASH = "grade_hash";

    private String gradeHash;
    private GradeEntry gradeEntry;
    private MainServiceHelper mainServiceHelper;

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

    private LinearLayout llOverviewWrapper;
    private TextView tvOverviewParticipants;
    private TextView tvOverviewAverage;
    private TextView tvOverviewSection1;
    private TextView tvOverviewSection2;
    private TextView tvOverviewSection3;
    private TextView tvOverviewSection4;
    private TextView tvOverviewSection5;

    private Button btnScrapeForOverview;
    private TextView tvOverviewNotPossible;
    private ProgressWheel progressWheel;
    private LinearLayout llRootView; // used to show snackbar

    private static final float DEFAULT_PROGRESS = 0.025f; // default progress, to indicate the progress bar
    private static final String IS_SCRAPING_STATE = "is_scraping_state";
    private static final String IS_OVERVIEW_POSSIBLE_STATE = "is_overview_possible_state";
    private static final String PROGRESS_STATE = "progress_state";
    private boolean isScraping;

    private boolean isOverviewPossible;

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

        // get views for overview
        llOverviewWrapper = (LinearLayout) findViewById(R.id.overview_wrapper);
        tvOverviewParticipants = (TextView) findViewById(R.id.tv_overview_participants);
        tvOverviewAverage = (TextView) findViewById(R.id.tv_overview_average);
        tvOverviewSection1 = (TextView) findViewById(R.id.tv_overview_section1);
        tvOverviewSection2 = (TextView) findViewById(R.id.tv_overview_section2);
        tvOverviewSection3 = (TextView) findViewById(R.id.tv_overview_section3);
        tvOverviewSection4 = (TextView) findViewById(R.id.tv_overview_section4);
        tvOverviewSection5 = (TextView) findViewById(R.id.tv_overview_section5);

        initScrapeForOverviewButton();
        tvOverviewNotPossible = (TextView) findViewById(R.id.tv_overview_not_possible);
        progressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        llRootView = (LinearLayout) findViewById(R.id.ll_root_view);

        // restore instance state if necessary
        if (savedInstanceState != null) {
            isOverviewPossible = savedInstanceState.getBoolean(IS_OVERVIEW_POSSIBLE_STATE);
            isScraping = savedInstanceState.getBoolean(IS_SCRAPING_STATE);
            if (isScraping) {
                progressWheel.setProgress(savedInstanceState.getFloat(PROGRESS_STATE, DEFAULT_PROGRESS));
                showProgressWheel();
            }
        } else {
            progressWheel.setProgress(DEFAULT_PROGRESS);
        }

        // register event bus
        EventBus.getDefault().register(this);

        // start intent to get data for Grade Detail page
        mainServiceHelper.getGradeDetails(gradeHash);
    }

    /**
     * Initialize ScrapeForOverview button.
     */
    private void initScrapeForOverviewButton() {
        btnScrapeForOverview = (Button) findViewById(R.id.btn_scrape_for_overview);

        btnScrapeForOverview.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: show loading animation and hide button
                if (gradeEntry != null) {
                    scrapeForOverview();
                }
            }
        });
    }

    /**
     * Starts the scraping process, hides the button and shows the ProgressWheel.
     */
    private void scrapeForOverview() {
        if (isOverviewPossible) {
            mainServiceHelper.scrapeForOverview(gradeEntry.getHash());
            btnScrapeForOverview.setVisibility(View.GONE);

            isScraping = true;
            showProgressWheel();
        }
    }

    /**
     * Hides the ProgressWheel and shows the button.
     */
    private void hideScrapeForOverview() {
        isScraping = false;

        progressWheel.setAnimation(null);
        progressWheel.setVisibility(View.GONE);

        if (isOverviewPossible) {
            btnScrapeForOverview.setVisibility(View.VISIBLE);
        } else {
            tvOverviewNotPossible.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows the progress wheel and start its animation.
     */
    private void showProgressWheel() {
        progressWheel.setVisibility(View.VISIBLE);
        progressWheel.startAnimation(AnimationUtils.loadAnimation(GradeDetailedActivity.this, R.anim.rotate_indefinitely));
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
        if (gradeEntry != null && overview.getGradeEntryHash().equals(gradeEntry.getHash())) {
            // hide button
            btnScrapeForOverview.setVisibility(View.GONE);

            llOverviewWrapper.setVisibility(View.VISIBLE);
            tvOverviewParticipants.setText(String.valueOf(overview.getParticipants()));
            writeDoubleToTextView(tvOverviewAverage, overview.getAverage());
            tvOverviewSection1.setText(String.valueOf(overview.getSection1()));
            tvOverviewSection2.setText(String.valueOf(overview.getSection2()));
            tvOverviewSection3.setText(String.valueOf(overview.getSection3()));
            tvOverviewSection4.setText(String.valueOf(overview.getSection4()));
            tvOverviewSection5.setText(String.valueOf(overview.getSection5()));
        }
    }

    /**
     * Receive an event when its determined whether it is possible to receive an overview.
     * @param overviewPossibleEvent
     */
    public void onEventMainThread(OverviewPossibleEvent overviewPossibleEvent) {
        if (!isScraping) {
            isOverviewPossible = overviewPossibleEvent.isOverviewPossible();

            if (isOverviewPossible) {
                btnScrapeForOverview.setVisibility(View.VISIBLE);
            } else {
                tvOverviewNotPossible.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Receive an event about the scraping progress and set the ProgressWheel accordingly.
     *
     * @param scrapeProgressEvent ScrapeProgressEvent
     */
    public void onEventMainThread(ScrapeProgressEvent scrapeProgressEvent) {
        if (progressWheel != null) {
            isScraping = true;

            int currentStep = scrapeProgressEvent.getCurrentStep();
            int stepCount = scrapeProgressEvent.getStepCount() - 1; // ignore table_overview step

            float progress = ((float) currentStep) / stepCount;
            progressWheel.setProgress(progress);

            if (currentStep == stepCount) {
                hideScrapeForOverview();
            }
        }
    }

    /**
     * Receive error events.
     *
     * @param errorEvent ErrorEvent
     */
    public void onEventMainThread(ErrorEvent errorEvent) {
        View.OnClickListener tryAgainListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrapeForOverview();
            }
        };

        switch (errorEvent.getType()) {
            case NO_NETWORK:
                showSnackbar("Keine Internetverbindung", tryAgainListener, "Nochmal");
                break;
            case TIMEOUT:
                showSnackbar("Zeitüberschreitung", tryAgainListener, "Nochmal");
                break;
            case GENERAL:
            default:
                showSnackbar("Allgemeiner Fehler", null, "FAQ");

        }

        hideScrapeForOverview();
    }

    /**
     * Shows a snackbar.
     *
     * @param text - text to show
     * @param action - OnClickListener
     * @param actionText - text for the OnClickListener
     */
    private void showSnackbar(String text, View.OnClickListener action, String actionText) {
        Snackbar.make(llRootView, text, Snackbar.LENGTH_LONG)
                .setAction(actionText, action)
                .show();
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
    protected void onDestroy() {
        super.onDestroy();
        // unregister EventBus
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(IS_SCRAPING_STATE, isScraping);
        outState.putFloat(PROGRESS_STATE, progressWheel.getProgress());
        outState.putBoolean(IS_OVERVIEW_POSSIBLE_STATE, isOverviewPossible);
    }
}
