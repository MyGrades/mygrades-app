package de.mygrades.main.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Log;

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
        Log.d(TAG, "Alarm canceled");

        // disable boot receiver
        packageManager.setComponentEnabledSetting(bootReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        Log.d(TAG, "BootReceiver disabled!");
    }
}
