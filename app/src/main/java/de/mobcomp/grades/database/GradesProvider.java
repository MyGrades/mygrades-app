package de.mobcomp.grades.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * ContentProvider to access the database.
 */
public class GradesProvider extends ContentProvider {
    private static final String TAG = GradesProvider.class.getSimpleName();

    private static final String AUTHORITY = "de.mobcomp.grades.provider";
    private static final String CONTENT_URI_STRING = "content://" + AUTHORITY;
    public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STRING);

    // IDs are used by the UriMatcher to discriminate URIs
    private static final int UNIVERSITY = 1;
    private static final int RULE = 2;
    private static final int ACTION = 3;
    private static final int ACTION_PARAM = 4;
    private static final int TRANSFORMER_MAPPING = 5;
    private static final int GRADE_ENTRY = 6;
    private static final int OVERVIEW = 7;

    // init UriMatcher
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, Database.University.TABLE, UNIVERSITY);
        uriMatcher.addURI(AUTHORITY, Database.Rule.TABLE, RULE);
        uriMatcher.addURI(AUTHORITY, Database.Action.TABLE, ACTION);
        uriMatcher.addURI(AUTHORITY, Database.ActionParam.TABLE, ACTION_PARAM);
        uriMatcher.addURI(AUTHORITY, Database.TransformerMapping.TABLE, TRANSFORMER_MAPPING);
        uriMatcher.addURI(AUTHORITY, Database.GradeEntry.TABLE, GRADE_ENTRY);
        uriMatcher.addURI(AUTHORITY, Database.Overview.TABLE, OVERVIEW);
    }

    // database
    private Database database;

    @Override
    public boolean onCreate() {
        database = new Database(getContext());
        database.open();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            default:
                Log.e(TAG, "Query, uri not supported: " + uri);
                throw new IllegalArgumentException("Query, uri not supported: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
            default:
                Log.e(TAG, "Insert, uri not supported: " + uri);
                throw new IllegalArgumentException("Insert, uri not supported: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            default:
                Log.e(TAG, "Delete, uri not supported: " + uri);
                throw new IllegalArgumentException("Delete, uri not supported: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            default:
                Log.e(TAG, "Update, uri not supported: " + uri);
                throw new IllegalArgumentException("Update, uri not supported: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
