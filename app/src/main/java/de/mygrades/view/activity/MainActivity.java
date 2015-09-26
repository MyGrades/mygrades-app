package de.mygrades.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.Random;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.database.dao.GradeEntry;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.GradesEvent;
import de.mygrades.util.Constants;
import de.mygrades.view.adapter.GradesRecyclerViewAdapter;
import de.mygrades.view.decoration.GradesDividerItemDecoration;
import de.mygrades.view.adapter.model.GradeItem;

/**
 * Activity to show the overview of grades.
 */
public class MainActivity extends AppCompatActivity {

    private Button btParse;
    private RecyclerView rvGrades;
    private GradesRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkLogin()) {
            goToUniversitySelection();
            return;
        }

        setContentView(R.layout.activity_main);

        // register event bus
        EventBus.getDefault().register(this);

        btParse = (Button) findViewById(R.id.bt_parse);
        btParse.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MainServiceHelper(MainActivity.this).scrapeForGrades();
            }
        });

        initGradesRecyclerView();
    }

    /**
     * Initialize recycler view and add dummy items.
     */
    private void initGradesRecyclerView() {
        rvGrades = (RecyclerView) findViewById(R.id.rv_grades);
        rvGrades.setLayoutManager(new LinearLayoutManager(rvGrades.getContext()));
        rvGrades.addItemDecoration(new GradesDividerItemDecoration(this, R.drawable.grade_divider, R.drawable.semester_divider));
        rvGrades.setItemAnimator(new DefaultItemAnimator());

        // set adapter
        adapter = new GradesRecyclerViewAdapter();
        rvGrades.setAdapter(adapter);
    }

    public void onEventMainThread(GradesEvent gradesEvent) {
        if (adapter != null) {
            for(GradeEntry gradeEntry : gradesEvent.getGrades()) {

                GradeItem item = new GradeItem();
                item.setName(gradeEntry.getName());

                Double creditPoints = gradeEntry.getCreditPoints();
                item.setCreditPoints(creditPoints == null ? null : creditPoints.floatValue());

                Double grade = gradeEntry.getGrade();
                item.setGrade(grade == null ? null : grade.floatValue());

                Random rand = new Random();
                adapter.addGradeForSemester(item, rand.nextInt((6 - 1) + 1) + 1);
            }
        }
    }

    /**
     * Checks if user is already logged in.
     *
     * @return true if user is logged in.
     */
    private boolean checkLogin() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(Constants.PREF_KEY_LOGGED_IN, false);
    }

    /**
     * Starts the SelectUniversityActivity.
     */
    private void goToUniversitySelection() {
        Intent intent = new Intent(this, SelectUniversityActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
