package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import de.mygrades.R;

/**
 * Activity to enter the username and password for the selected university.
 */
public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_UNIVERSITY_NAME = "university_name";
    public static final String EXTRA_UNIVERSITY_ID = "university_id";

    private String universityName;
    private long universityId;

    private TextView tvUniversityName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get extra data
        Bundle extras = getIntent().getExtras();
        universityName = extras.getString(EXTRA_UNIVERSITY_NAME, "");
        universityId = extras.getLong(EXTRA_UNIVERSITY_ID, 0);

        // set university name
        tvUniversityName = (TextView) findViewById(R.id.tv_university_name);
        tvUniversityName.setText(universityName);
    }
}
