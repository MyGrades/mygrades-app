package de.mygrades.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;

/**
 * Activity to enter the username and password for the selected university.
 */
public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_UNIVERSITY_NAME = "university_name";
    public static final String EXTRA_UNIVERSITY_ID = "university_id";

    private String universityName;
    private long universityId;

    private TextView tvUniversityName;
    private Button btLogin;

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

        // login button
        btLogin = (Button) findViewById(R.id.bt_login);
        btLogin.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(intent);
            }
        });

        // load detailed university
        MainServiceHelper mainServiceHelper = new MainServiceHelper(this);
        mainServiceHelper.getDetailedUniversity(universityId);
    }
}
