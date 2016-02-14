package de.mygrades.view.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.XpPreferenceFragment;
import android.support.v7.widget.PreferenceDividerDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.webkit.WebView;

import net.xpece.android.support.preference.ListPreference;

import de.mygrades.BuildConfig;
import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.alarm.ScrapeAlarmManager;
import de.mygrades.util.Config;
import de.mygrades.util.LogoutHelper;

/**
 * Created by tilman on 12.12.15.
 */
public class SettingsFragment extends XpPreferenceFragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private SharedPreferences sharedPreferences;
    private ScrapeAlarmManager scrapeAlarmManager;

    @Override
    public void onCreatePreferences2(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        initMaxCreditPointsPreference();
        initLogoutPreference();
        initBuildVersionPreference();
        initSourceCodePreference();
        initLicensePreference();
        initOpenSourceLicenses();
        initLegalNotice();

        // notification preferences
        scrapeAlarmManager = new ScrapeAlarmManager(getContext());
        initAutomaticScrapingPreference();
        initScrapeFrequencyPreference();
    }

    @Override
    public void onRecyclerViewCreated(RecyclerView list) {
        list.addItemDecoration(new PreferenceDividerDecoration(getContext()).drawBottom(true));
    }

    /**
     * Initializes the automatic scraping preference and sets an alarm.
     */
    private void initAutomaticScrapingPreference() {
        Preference automaticScrapingPreference = findPreference(getString(R.string.pref_key_automatic_scraping));

        // set change listener to set alarm
        automaticScrapingPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                boolean boolValue = (boolean) value;
                if (boolValue) {
                    scrapeAlarmManager.setAlarmFromPrefs(true, true);
                } else {
                    scrapeAlarmManager.cancelAlarm();
                }
                return true;
            }
        });
    }

    /**
     * Initializes the scrape frequency preference, updates the summary and sets an alarm.
     */
    private void initScrapeFrequencyPreference() {
        Preference scrapeFrequencyPreference = findPreference(getString(R.string.pref_key_scrape_frequency));

        // set summary
        scrapeFrequencyPreference.setSummary(getDisplayValue(
                        (ListPreference) scrapeFrequencyPreference,
                        sharedPreferences.getString(getString(R.string.pref_key_scrape_frequency), "")
        ));

        // set change listener to update summary and set alarm
        scrapeFrequencyPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                // Set the summary to reflect the new value.
                preference.setSummary(getDisplayValue((ListPreference) preference, value.toString()));

                scrapeAlarmManager.setAlarm(Integer.parseInt(value.toString()));
                return true;
            }
        });
    }

    /**
     * Gets the display value of the given value for listPreference.
     * @param listPreference current ListPreference
     * @param value value of which the display value is needed
     * @return display value of given value
     */
    private CharSequence getDisplayValue(ListPreference listPreference, String value) {
        int index = listPreference.findIndexOfValue(value);

        // Set the summary to reflect the new value.
        return index >= 0 ? listPreference.getEntries()[index] : null;
    }

    /**
     * Initializes the maximum credit points preference and validates the input.
     */
    private void initMaxCreditPointsPreference() {
        Preference maxCreditPointsPreference = findPreference(getString(R.string.pref_key_max_credit_points));

        // set summary
        maxCreditPointsPreference.setSummary(sharedPreferences.getString(
                getString(R.string.pref_key_max_credit_points), ""
        ));

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
                LogoutHelper logoutHelper = new LogoutHelper(getActivity());
                logoutHelper.showDialog();

                return true;
            }
        });
    }

    /**
     * Sets the current build version name as the summary.
     */
    private void initBuildVersionPreference() {
        Preference buildVersion = findPreference(getString(R.string.pref_key_build_version));
        buildVersion.setSummary(BuildConfig.VERSION_NAME);
    }

    /**
     * Open browser with link to source code.
     */
    private void initSourceCodePreference() {
        Preference sourceCode = findPreference(getString(R.string.pref_key_github));
        sourceCode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Uri uri = Uri.parse(getString(R.string.uri_source_code));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return false;
            }
        });
    }

    /**
     * Show license in dialog.
     */
    private void initLicensePreference() {
        Preference license = findPreference(getString(R.string.pref_key_license));
        license.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.license))
                    .setTitle(getString(R.string.pref_license_title))
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
                    .show();
                return false;
            }
        });
    }

    /**
     * Show open source licenses in dialog.
     */
    private void initOpenSourceLicenses() {
        Preference openSourceLicenses = findPreference(getString(R.string.pref_key_third_party_license));
        openSourceLicenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                WebView view = (WebView) LayoutInflater.from(getContext()).inflate(R.layout.dialog_licenses, null);
                view.loadUrl("file:///android_asset/open_source_licenses.html");
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.pref_third_party_license_title))
                        .setView(view)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return false;
            }
        });
    }

    /**
     * Show legal notice in dialog.
     */
    private void initLegalNotice() {
        Preference legalNotice = findPreference(getString(R.string.pref_key_legal_notice));
        legalNotice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                WebView view = (WebView) LayoutInflater.from(getContext()).inflate(R.layout.dialog_licenses, null);
                view.loadUrl(Config.getServerUrl() + "/impressum");
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.pref_legal_notice_title))
                        .setView(view)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return false;
            }
        });
    }
}
