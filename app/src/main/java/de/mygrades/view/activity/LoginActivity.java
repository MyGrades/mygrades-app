package de.mygrades.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.util.Constants;

/**
 * Activity to enter the username and password for the selected university.
 */
public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_UNIVERSITY_NAME = "university_name";
    public static final String EXTRA_UNIVERSITY_ID = "university_id";

    private String universityName;
    private long universityId;

    private TextView tvUniversityName;
    private EditText etUsername;
    private EditText etPassword;
    private Button btLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.toolbar_login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get extra data
        if (getIntent() != null && getIntent().getExtras() != null) {
            universityName = getIntent().getExtras().getString(EXTRA_UNIVERSITY_NAME, "");
            universityId = getIntent().getExtras().getLong(EXTRA_UNIVERSITY_ID, 0);
        } else {
            // TODO: load universityName, universityId from prefs / database (and maybe the username)
        }

        // set university name
        tvUniversityName = (TextView) findViewById(R.id.tv_university_name);
        tvUniversityName.setText(universityName);

        etUsername = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_passwort);

        initLoginButton();

        // load detailed university
        MainServiceHelper mainServiceHelper = new MainServiceHelper(this);
        mainServiceHelper.getDetailedUniversity(universityId);
    }

    /**
     * Initialize login button.
     */
    private void initLoginButton() {
        btLogin = (Button) findViewById(R.id.bt_login);
        btLogin.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInput()) {
                    saveSelectedUniversity();
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
    private boolean checkInput() {
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
     * Saves universityId and logged-in state to preferences.
     */
    private void saveSelectedUniversity() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(Constants.PREF_KEY_UNIVERSITY_ID, universityId);
        editor.putBoolean(Constants.PREF_KEY_INITIAL_LOADING_DONE, false);
        editor.apply();
    }

    /**
     * Save login data and start scraping through main service.
     */
    private void loginAndScrapeForGrades() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        MainServiceHelper mainServiceHelper = new MainServiceHelper(LoginActivity.this);
        mainServiceHelper.login(username, password);
    }

    /**
     * Starts an intent to go to the main activity.
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
}
