package de.mygrades.view.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.XpPreferenceFragment;
import android.support.v7.widget.PreferenceDividerDecoration;
import android.support.v7.widget.RecyclerView;

import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;

/**
 * Created by tilman on 12.12.15.
 */
public class SettingsFragment extends XpPreferenceFragment {

    @Override
    public void onCreatePreferences2(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);

        initMaxCreditPointsPreference();
        initLogoutPreference();
    }

    @Override
    public void onRecyclerViewCreated(RecyclerView list) {
        list.addItemDecoration(new PreferenceDividerDecoration(getContext()).drawBottom(true));
    }

    /**
     * Initializes the maximum credit points preference and validates the input.
     */
    private void initMaxCreditPointsPreference() {
        Preference maxCreditPointsPreference = findPreference(getString(R.string.pref_key_max_credit_points));

        // set summary
        maxCreditPointsPreference.setSummary(PreferenceManager
                .getDefaultSharedPreferences(getContext())
                .getString(getString(R.string.pref_key_max_credit_points), ""));

        // set change listener to validate input and update summary
        maxCreditPointsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                if (value.toString().startsWith("0")) {
                    return false;
                }

                try {
                    int maxCreditPoints = Integer.parseInt(value.toString());
                    preference.setSummary("" + maxCreditPoints);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });
    }

    /**
     * Initializes the logout dialog.
     */
    private void initLogoutPreference() {
        Preference logout = findPreference(getString(R.string.pref_key_logout));
        logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(R.string.dialog_logout_message);
                builder.setTitle(getString(R.string.dialog_logout_title));
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                });

                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        });
    }

    /**
     * Logout user and goes to SelectUniversityActivity.
     * The user cannot return to the MainActivity by pressing the back-button.
     */
    private void logout() {
        MainServiceHelper mainServiceHelper = new MainServiceHelper(getContext());
        mainServiceHelper.logout();

        Intent intent = new Intent(getContext(), SelectUniversityActivity.class);
        // set flags, so the user won't be able to go back to the main activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
