package de.mygrades.main.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import de.mygrades.R;

/**
 * Receiver for BOOT_COMPLETED.
 * Gets only called if there was an scheduled alarm before reboot
 * (is disabled by default -> see manifest).
 * Sets alarm accordingly to settings in shared prefs.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Boot completed received. -> set alarm");

        // get interval from shared prefs
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int intervalMinutes = Integer.parseInt(prefs.getString(
                context.getResources().getString(R.string.pref_key_scrape_frequency), "-1"
        ));

        // set alarm
        ScrapeAlarmManager scrapeAlarmManager = new ScrapeAlarmManager(context);
        scrapeAlarmManager.setAlarm(intervalMinutes);
    }
}
