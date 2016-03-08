package de.mygrades.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import de.mygrades.database.dao.ActionDao;
import de.mygrades.database.dao.ActionParamDao;
import de.mygrades.database.dao.DaoMaster;
import de.mygrades.database.dao.GradeEntry;
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

        // important: old/new version refers to database schema version, not the app version
        if (oldVersion < 2) {
            reset(db);
        } else if (oldVersion == 2 && newVersion == 3) {
            upgrade2to3(db);
        }
    }

    private void upgrade2to3(SQLiteDatabase db) {
        // update GradeEntry hash
        updateGradeEntryHash(db);
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

    /**
     * Updates all GradeEntry hashes (primary keys).
     * 'Attempt' is appended to the existing hash to provide better distinguishable hash values.
     *
     * @param db SQLiteDatabase
     */
    private void updateGradeEntryHash(SQLiteDatabase db) {
        // select all grade entries
        String sql = "SELECT exam_id, name, semester, attempt, hash from GRADE_ENTRY;";
        Cursor c = db.rawQuery(sql, new String[] {});

        while (c.moveToNext()) {
            String examId = c.getString(c.getColumnIndex("EXAM_ID"));
            String semester = c.getString(c.getColumnIndex("SEMESTER"));
            String name = c.getString(c.getColumnIndex("NAME"));
            String attempt = c.getString(c.getColumnIndex("ATTEMPT"));
            String oldHash = c.getString(c.getColumnIndex("HASH"));

            // create new hash
            String hash = (examId == null ? "" : examId) +
                    (semester == null ? "" : semester) +
                    (name == null ? "" : name) +
                    (attempt == null ? "" : attempt);
            hash = GradeEntry.toBase64(hash);

            // update hash in database
            ContentValues values = new ContentValues();
            values.put("HASH", hash);
            db.update("GRADE_ENTRY", values, "HASH = ?", new String[]{oldHash});
        }
        c.close();
    }
}
