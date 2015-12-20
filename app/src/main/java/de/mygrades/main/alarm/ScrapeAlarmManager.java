package de.mygrades.main.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import de.mygrades.R;

/**
 * Manages all alarm operations to repeat scraping automatically.
 */
public class ScrapeAlarmManager {
    private static final String TAG = ScrapeAlarmManager.class.getSimpleName();

    private static final int STANDARD_INTERVAL = 120;

    private AlarmManager alarmManager;
    private Context context;

    // needed to enable / disable boot receiver
    private ComponentName bootReceiver;
    private PackageManager packageManager;

    public ScrapeAlarmManager(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        this.bootReceiver = new ComponentName(this.context, BootReceiver.class);
        this.packageManager = this.context.getPackageManager();
    }

    /**
     * Set an alarm for given interval from shared preferences.
     * @param override override an existing alarm?
     * @param forceSet force setting of alarm, even if it's not (yet) in shared prefs enabled
     */
    public void setAlarmFromPrefs(boolean override, boolean forceSet) {
        // only if scraping is activated in shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isActiveAutomaticScraping = prefs.getBoolean(
                context.getResources().getString(R.string.pref_key_automatic_scraping), false
        );
        if (!isActiveAutomaticScraping && !forceSet) {
            Log.d(TAG, "Automatic Scraping not enabled in Shared Preferences. Do nothing.");
            return;
        }

        // check if there is an alarm set
        if (!override) {
            Intent intent = new Intent(context, AlarmReceiver.class);
            boolean isSet = (PendingIntent.getBroadcast(context, 0, intent,
                    PendingIntent.FLAG_NO_CREATE) != null);
            if (isSet) {
                Log.d(TAG, "Alarm already set. Do nothing.");
                return;
            }
        }

        // get setting from shared preferences
        int intervalMinutes = Integer.parseInt(prefs.getString(
                context.getResources().getString(R.string.pref_key_scrape_frequency), "-1"
        ));

        // set alarm accordingly to user's settings
        setAlarm(intervalMinutes);
    }

    /**
     * Set an alarm for given interval in minutes.
     * If there is an existing alarm it will get overwritten.
     * @param intervalMinutes interval in minutes
     */
    public void setAlarm(int intervalMinutes) {
        if (intervalMinutes == -1) {
            intervalMinutes = STANDARD_INTERVAL;
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // minutes * 60 secs * 1000 msec
        long interval = intervalMinutes * 60 * 1000;
        // first alarm triggers minimum 10 minutes after being set
        long trigger = 10 * 60 * 1000;

        // we can't use setInexactRepeating, because of the rare predefined intervals
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + trigger, interval, alarmIntent);
        Log.d(TAG, "Alarm set. Interval: " + interval + ", Trigger: " + trigger);

        // enable boot receiver to set alarm after reboot
        packageManager.setComponentEnabledSetting(bootReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        Log.d(TAG, "BootReceiver enabled!");
    }

    /**
     * Cancel alarm.
     * It will never fire again until a new one is set.
     */
    public void cancelAlarm() {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.cancel(alarmIntent);
        alarmIntent.cancel();
        Log.d(TAG, "Alarm canceled");

        // disable boot receiver
        packageManager.setComponentEnabledSetting(bootReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        Log.d(TAG, "BootReceiver disabled!");
    }
}
