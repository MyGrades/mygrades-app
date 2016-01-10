package de.mygrades.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import de.mygrades.database.dao.ActionDao;
import de.mygrades.database.dao.ActionParamDao;
import de.mygrades.database.dao.DaoMaster;
import de.mygrades.database.dao.GradeEntryDao;
import de.mygrades.database.dao.OverviewDao;
import de.mygrades.database.dao.RuleDao;
import de.mygrades.database.dao.TransformerMappingDao;
import de.mygrades.database.dao.UniversityDao;
import de.mygrades.main.processor.LoginProcessor;

/**
 * DatabaseHelper to migrate database tables at version changes.
 */
public class DatabaseHelper extends DaoMaster.OpenHelper {
    private Context context;

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion);

        if (oldVersion < 6) {
            reset(db);
        }
    }

    /**
     * Resets database and all preferences.
     * Should be used as a fallback if no migrations are supplied.
     *
     * @param db SQLiteDatabase
     */
    private void reset(SQLiteDatabase db) {
        LoginProcessor loginProcessor = new LoginProcessor(context);
        loginProcessor.deletePreferences();

        dropAllTables(db);
        onCreate(db);
    }

    /**
     * Drops all tables.
     *
     * @param db SQLiteDatabase
     */
    private void dropAllTables(SQLiteDatabase db) {
        boolean ifExists = true;
        UniversityDao.dropTable(db, ifExists);
        RuleDao.dropTable(db, ifExists);
        ActionDao.dropTable(db, ifExists);
        ActionParamDao.dropTable(db, ifExists);
        TransformerMappingDao.dropTable(db, ifExists);
        GradeEntryDao.dropTable(db, ifExists);
        OverviewDao.dropTable(db, ifExists);
    }
}
