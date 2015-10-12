package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.database.dao.GradeEntry;
import de.mygrades.database.dao.Overview;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.GradeEntryEvent;
import de.mygrades.main.events.OverviewEvent;
import de.mygrades.main.events.OverviewPossibleEvent;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_detailed);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.toolbar_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // register event bus
        EventBus.getDefault().register(this);

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
                    mainServiceHelper.scrapeForOverview(gradeEntry.getHash());
                }
            }
        });
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
     * Receive an event when its determined whether it is possible to receive a overview.
     * @param overviewPossibleEvent
     */
    public void onEventMainThread(OverviewPossibleEvent overviewPossibleEvent) {
        if (overviewPossibleEvent.isOverviewPossible()) {
            btnScrapeForOverview.setVisibility(View.VISIBLE);
        } else {
            tvOverviewNotPossible.setVisibility(View.VISIBLE);
        }
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
}
