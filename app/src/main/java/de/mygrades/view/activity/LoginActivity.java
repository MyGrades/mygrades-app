package de.mygrades.view.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
    public static final String EXTRA_USERNAME = "username";

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
            universityId = getIntent().getExtras().getLong(EXTRA_UNIVERSITY_ID, 0);

            universityName = getIntent().getExtras().getString(EXTRA_UNIVERSITY_NAME, "");
            tvUniversityName.setText(universityName == null ? "" : universityName);

            // if user name is set, it means the user came from the FragmentInitialScraping
            String username = getIntent().getExtras().getString(EXTRA_USERNAME, null);
            if(username != null) {
                etUsername.setText(username);
            } else {
                navigateUpFromSameTask = true;
            }
        }

        mainServiceHelper.getDetailedUniversity(universityId);
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
                    // hide keyboard
                    InputMethodManager im = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

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

        // immediately save the universityId to shared preferences, because it gets checked in the
        // MainActivity as an indicator that the user is logged in.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Constants.PREF_KEY_UNIVERSITY_ID, universityId);
        editor.apply();

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
}
