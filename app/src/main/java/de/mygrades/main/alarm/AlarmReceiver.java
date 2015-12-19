package de.mygrades.main.alarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import de.mygrades.main.MainServiceHelper;

/**
 * Receiver for alarms from AlarmManager. Operates on main thread!
 * Starts scraping automatically.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Alarm received");

        MainServiceHelper mainServiceHelper = new MainServiceHelper(context);
        startWakefulService(context, mainServiceHelper.getIntentForScrapeForGrades(false, true));
    }
}
