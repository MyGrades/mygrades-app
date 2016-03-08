package de.mygrades;

import android.app.Application;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import de.mygrades.database.DatabaseHelper;
import de.mygrades.database.dao.DaoMaster;
import de.mygrades.database.dao.DaoSession;
import de.mygrades.main.alarm.ScrapeAlarmManager;

/**
 * MyGradesApplication to hold the DaoSession in application scope.
 */
public class MyGradesApplication extends Application {
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        // set preferences default values
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        SQLiteOpenHelper helper = new DatabaseHelper(this, "mygrades.db", null);
        DaoMaster daoMaster = new DaoMaster(helper.getWritableDatabase());
        daoSession = daoMaster.newSession();

        // check if alarm for automatic scraping is set
        ScrapeAlarmManager scrapeAlarmManager = new ScrapeAlarmManager(this);
        scrapeAlarmManager.setAlarmFromPrefs(false, false);
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
