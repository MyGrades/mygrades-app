package de.mygrades;

import android.app.Application;
import android.database.sqlite.SQLiteOpenHelper;

import de.mygrades.database.dao.DaoMaster;
import de.mygrades.database.dao.DaoSession;

/**
 * MyGradesApplication to hold the DaoSession in application scope.
 */
public class MyGradesApplication extends Application {
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        SQLiteOpenHelper helper = new DaoMaster.DevOpenHelper(this, "mygrades-db", null);
        DaoMaster daoMaster = new DaoMaster(helper.getWritableDatabase());
        daoSession = daoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
