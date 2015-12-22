package de.mygrades.main.alarm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Receiver for alarms from AlarmManager. Operates on main thread!
 * Starts scraping automatically.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received");

        if (wifiOnly(context)) {
            if (!wifiConnected(context)) {
                Log.d(TAG, "No wifi. Eventually try again.");
                ScrapeAlarmManager scrapeAlarmManager = new ScrapeAlarmManager(context);
                scrapeAlarmManager.setOneTimeFallbackAlarm(false);
                return;
            }
        }

        MainServiceHelper mainServiceHelper = new MainServiceHelper(context);
        startWakefulService(context, mainServiceHelper.getIntentForScrapeForGrades(false, true));
    }

    /**
     * Checks if wifi only is selected in the preferences.
     *
     * @param context - Context
     * @return true, if wifi only is selected
     */
    private boolean wifiOnly(Context context) {
        SharedPreferences prefs = getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_key_only_wifi);
        return prefs.getBoolean(key, true);
    }

    /**
     * Checks if the current active network is WIFI.
     *
     * @param context - Context
     * @return true, if current active network is WIFI.
     */
    private boolean wifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
