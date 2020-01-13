package dh.mygrades.main.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * PackageReplacedReceiver is called after an app upgrade and recreates the alarm settings
 * to receive notifications for new grades.
 */
public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // set alarm
        ScrapeAlarmManager scrapeAlarmManager = new ScrapeAlarmManager(context);
        scrapeAlarmManager.setAlarmFromPrefs(true, false);
    }
}
