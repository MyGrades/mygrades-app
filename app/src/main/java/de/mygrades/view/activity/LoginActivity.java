package de.mygrades.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.LoginDataEvent;

/**
 * Activity to enter the username and password for the selected university.
 */
public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_UNIVERSITY_NAME = "university_name";
    public static final String EXTRA_UNIVERSITY_ID = "university_id";

    private String universityName;
    private long universityId;
    private boolean navigateUpFromSameTask;

    // views
    private TextView tvUniversityName;
    private EditText etUsername;
    private EditText etPassword;
    private Button btLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // load detailed university
        MainServiceHelper mainServiceHelper = new MainServiceHelper(this);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.toolbar_login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvUniversityName = (TextView) findViewById(R.id.tv_university_name);
        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_passwort);

        initLoginButton();

        // get extra data
        if (getIntent() != null && getIntent().getExtras() != null) {
            universityName = getIntent().getExtras().getString(EXTRA_UNIVERSITY_NAME, "");
            universityId = getIntent().getExtras().getLong(EXTRA_UNIVERSITY_ID, 0);
            navigateUpFromSameTask = true;
        } else {
            mainServiceHelper.getLoginDataFromDatabase();
        }

        tvUniversityName.setText(universityName == null ? "" : universityName);
        mainServiceHelper.getDetailedUniversity(universityId);

        // register to events
        EventBus.getDefault().register(this);
    }

    /**
     * Receive a LoginDataEvent with previously entered username and selected university.
     *
     * @param loginDataEvent LoginDataEvent
     */
    public void onEventMainThread(LoginDataEvent loginDataEvent) {
        this.universityId = loginDataEvent.getUniversityId();
        this.universityName = loginDataEvent.getUniversityName();

        tvUniversityName.setText(universityName);
        etUsername.setText(loginDataEvent.getUsername());
    }

    /**
     * Initialize the login button.
     */
    private void initLoginButton() {
        btLogin = (Button) findViewById(R.id.bt_login);
        btLogin.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    loginAndScrapeForGrades();
                    goToMainActivity();
                }
            }
        });
    }

    /**
     * Checks if username or password are empty and shows an error message.
     *
     * @return true if input is correct
     */
    private boolean validateInput() {
        boolean inputCorrect = true;

        if (TextUtils.isEmpty(etUsername.getText().toString())) {
            etUsername.setError(getResources().getString(R.string.error_username_not_empty));
            inputCorrect = false;
        }

        if (TextUtils.isEmpty(etPassword.getText().toString())) {
            etPassword.setError(getResources().getString(R.string.error_password_not_empty));
            inputCorrect = false;
        }

        return inputCorrect;
    }

    /**
     * Saves the selected universityId and the login data in a background thread.
     * The encryption may take some time and afterwards the initial scraping is tarted.
     */
    private void loginAndScrapeForGrades() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        MainServiceHelper mainServiceHelper = new MainServiceHelper(LoginActivity.this);
        mainServiceHelper.loginAndScrapeForGrades(username, password, universityId);
    }

    /**
     * Starts an intent to go to the MainActivity.
     */
    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        // set flags, so the user won't be able to go back to the login activity
        intent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        LoginActivity.this.startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                if (navigateUpFromSameTask) {
                    NavUtils.navigateUpFromSameTask(this);
                } else {
                    Intent intent = new Intent(this, SelectUniversityActivity.class);
                    startActivity(intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
