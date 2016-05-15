package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import de.mygrades.R;

/**
 * Created by tilman on 12.12.15.
 */
public class SettingsActivity extends AppCompatActivity {

    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            settingsFragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.content, settingsFragment, "settings").commit();
        } else {
            settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("settings");
        }

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.toolbar_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }
}
