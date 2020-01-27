package dh.mygrades;

import android.app.Application;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import dh.mygrades.R;
import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import dh.mygrades.database.DatabaseHelper;
import dh.mygrades.database.dao.DaoMaster;
import dh.mygrades.database.dao.DaoSession;
import dh.mygrades.main.alarm.ScrapeAlarmManager;

/**
 * MyGradesApplication to hold the DaoSession in application scope.
 */
public class MyGradesApplication extends Application {
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        // Init Sentry Bug Tracking
        if (!BuildConfig.DEBUG) {
            Sentry.init("https://9d668d239d8c435182b234391c0a1ce6@sentry.io/2014696?release=" + BuildConfig.VERSION_CODE, new AndroidSentryClientFactory(this));
        }


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
