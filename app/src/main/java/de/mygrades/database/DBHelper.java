package de.mygrades.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import java.util.List;

import de.mygrades.main.model.University;

/**
 * Helper class to access the database with convenience methods.
 */
public class DBHelper {
    private Context context;
    private ContentResolver contentResolver;

    private static final Uri UNIVERSITY_URI;
    private static final Uri RULE_URI;
    private static final Uri ACTION_URI;
    private static final Uri ACTION_PARAM_URI;
    private static final Uri TRANSFORMER_MAPPING_URI;
    private static final Uri GRADE_ENTRY_URI;
    private static final Uri OVERVIEW_URI;

    static {
        Uri contentUri = GradesProvider.CONTENT_URI;

        UNIVERSITY_URI = contentUri.buildUpon().appendPath(Database.University.TABLE).build();
        RULE_URI = contentUri.buildUpon().appendPath(Database.Rule.TABLE).build();
        ACTION_URI = contentUri.buildUpon().appendPath(Database.Action.TABLE).build();
        ACTION_PARAM_URI = contentUri.buildUpon().appendPath(Database.ActionParam.TABLE).build();
        TRANSFORMER_MAPPING_URI = contentUri.buildUpon().appendPath(Database.TransformerMapping.TABLE).build();
        GRADE_ENTRY_URI = contentUri.buildUpon().appendPath(Database.GradeEntry.TABLE).build();
        OVERVIEW_URI = contentUri.buildUpon().appendPath(Database.Overview.TABLE).build();
    }

    public DBHelper(Context context) {
        this.context = context.getApplicationContext();
        this.contentResolver = this.context.getContentResolver();
    }

    /**
     * Returns a sorted university CursorLoader.
     *
     * @return CursorLoader
     */
    public CursorLoader getUniversityLoader() {
        // select columns
        String[] projection = {
                Database.University.ID,
                Database.University.NAME,
                Database.University.SHORT_NAME,
                Database.University.UNIVERSITY_ID,
                Database.University.UPDATED_AT_SERVER
        };

        // set sort order
        String sortOder = Database.University.NAME + " COLLATE NOCASE ASC";

        return new CursorLoader(context, UNIVERSITY_URI, projection, null, null, sortOder);
    }

    /**
     * Inserts a list of universities (without detailed information) into the database.
     *
     * @param universities - list of universities
     */
    public void createUniversities(List<University> universities) {
        for(University university : universities) {
            ContentValues values = new ContentValues();
            values.put(Database.University.UNIVERSITY_ID, university.getUniversityId());
            values.put(Database.University.SHORT_NAME, university.getShortName());
            values.put(Database.University.NAME, university.getName());
            values.put(Database.University.UPDATED_AT_SERVER, university.getUpdatedAtServer());

            // insert into database
            contentResolver.insert(UNIVERSITY_URI, values);
        }

        // notify loaders about change even if no universities were added
        context.getContentResolver().notifyChange(UNIVERSITY_URI, null);
    }
}
