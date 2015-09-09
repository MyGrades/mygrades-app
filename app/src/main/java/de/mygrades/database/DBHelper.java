package de.mygrades.database;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

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

    // TODO: implement convenience methods for database queries, inserts, etc...
}
