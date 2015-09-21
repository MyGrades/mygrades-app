package de.mygrades.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.util.Constants;

/**
 * Activity to show the overview of grades.
 */
public class MainActivity extends AppCompatActivity {

    private Button btParse;

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
