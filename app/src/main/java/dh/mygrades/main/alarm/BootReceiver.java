package dh.mygrades.main.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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

        // set alarm
        ScrapeAlarmManager scrapeAlarmManager = new ScrapeAlarmManager(context);
        scrapeAlarmManager.setAlarmFromPrefs(true, false);
    }
}
