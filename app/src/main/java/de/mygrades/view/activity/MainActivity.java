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

import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.util.Constants;
import de.mygrades.view.adapter.GradesRecyclerViewAdapter;
import de.mygrades.view.decoration.GradesDividerItemDecoration;
import de.mygrades.view.model.GradeItem;
import de.mygrades.view.model.SemesterItem;

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
        adapter = new GradesRecyclerViewAdapter();
        rvGrades.setAdapter(adapter);

        adapter.add(new SemesterItem(1, "Wintersemester 2012/13", 1.00f, 5), 0);
        adapter.add(new GradeItem("Programmieren 1", 1.0f, 3.5f), 1);
        adapter.add(new GradeItem("Programmieren 1 - Praktikum", 1.0f, 1.5f), 2);
        adapter.add(new SemesterItem(2, "Sommersemester 2013", 1.00f, 30), 3);
        adapter.add(new GradeItem("Algorithmen und Datenstrukturen", 1.3f, 3.5f), 4);
        adapter.add(new GradeItem("Algorithmen und Datenstrukturen - Praktikum", 1.0f, 1.5f), 5);
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
}
