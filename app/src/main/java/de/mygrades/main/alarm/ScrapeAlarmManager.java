package de.mygrades.main.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

    public ScrapeAlarmManager(Context context) {
        this.context = context.getApplicationContext();
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
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
        // first alarm triggers 10 minutes after being set
        long trigger = 10 * 60 * 1000;

        // TODO: only for testing purposes
        interval = 1 * 60 * 1000;
        trigger = 1 * 60 * 1000;

        // we can't use setInexactRepeating, because of the rare predefined intervals
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + trigger, interval, alarmIntent);
        Log.d(TAG, "Alarm set. Interval: " + interval);
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
    }
}
