package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;

/**
 * Created by jonastheis on 03.10.15.
 */
public class GradeDetailedActivity extends AppCompatActivity {
    private static final String TAG = GradeDetailedActivity.class.getSimpleName();

    public static final String EXTRA_GRADE_HASH = "grade_hash";

    private String gradeHash;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade_detailed);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.toolbar_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get extra data
        Bundle extras = getIntent().getExtras();
        gradeHash = extras.getString(EXTRA_GRADE_HASH, "");
        Log.d(TAG, gradeHash);

        // set Click listener to temp button
        Button button = (Button) findViewById(R.id.bt_go);
        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainServiceHelper mainServiceHelper = new MainServiceHelper(GradeDetailedActivity.this);
                mainServiceHelper.scrapeForOverview(gradeHash);
            }
        });
    }
}
