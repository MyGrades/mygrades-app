package de.mygrades.main.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receiver for alarms from AlarmManager. Operates on main thread!
 * Starts scraping automatically.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Alarm received");
        //TODO: WakefulBroadcastReceiver and start scraping
    }
}
