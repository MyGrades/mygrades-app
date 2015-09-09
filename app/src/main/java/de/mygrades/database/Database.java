package de.mygrades.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Create all database tables.
 */
public class Database {

    private static final String DATABASE_NAME = "mygrades.db";
    private static final int DATABASE_VERSION = 1;

    public SQLiteDatabase db;
    private DBHelper dbHelper;

    public Database(Context context) {
        dbHelper = new DBHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Helper class to
     * - create tables
     * - open and close the database connection
     * - perform database migrations between version upgrades
     */
    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create tables
            db.execSQL(University.CREATE_TABLE);
            db.execSQL(Rule.CREATE_TABLE);
            db.execSQL(Action.CREATE_TABLE);
            db.execSQL(ActionParam.CREATE_TABLE);
            db.execSQL(TransformerMapping.CREATE_TABLE);
            db.execSQL(GradeEntry.CREATE_TABLE);
            db.execSQL(Overview.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // migrate database to new version
        }
    }

    /**
     * Open database connection.
     * @throws SQLiteException
     */
    public void open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = dbHelper.getReadableDatabase();
        }
    }

    /**
     * University table.
     */
    public class University {
        public static final String TABLE = "university";

        // columns
        public static final String ID = "_id";
        public static final String UNIVERSITY_ID = "university_id";
        public static final String SHORT_NAME = "short_name";
        public static final String NAME = "name";
        public static final String UPDATED_AT_SERVER = "updated_at_server";

        // create table
        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + UNIVERSITY_ID + " INTEGER NOT NULL UNIQUE, "
                + SHORT_NAME + " TEXT NOT NULL, "
                + NAME + " TEXT NOT NULL, "
                + UPDATED_AT_SERVER + " TEXT "
                + ");";
    }

    /**
     * Rule table.
     */
    public class Rule {
        public static final String TABLE = "rule";

        // columns
        public static final String ID = "_id";
        public static final String RULE_ID = "rule_id";
        public static final String TYPE = "type";
        public static final String UPDATED_AT = "updated_at";

        // create table
        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RULE_ID + " INTEGER NOT NULL UNIQUE, "
                + TYPE + " TEXT NOT NULL, "
                + UPDATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP "
                + ");";
    }

    /**
     * Action table.
     */
    public class Action {
        public static final String TABLE = "action";

        // columns
        public static final String ID = "_id";
        public static final String ACTION_ID = "action_id";
        public static final String RULE_ID = "rule_id";
        public static final String POSITION = "position";
        public static final String METHOD = "method";
        public static final String URL = "url";
        public static final String PARSE_EXPRESSION = "parse_expression";
        public static final String PARSE_TYPE = "parse_type";

        // create table
        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ACTION_ID + " INTEGER NOT NULL UNIQUE, "
                + RULE_ID + " INTEGER NOT NULL, "
                + POSITION + " INTEGER NOT NULL, "
                + METHOD + " TEXT NOT NULL, "
                + URL + " TEXT, "
                + PARSE_EXPRESSION + " TEXT, "
                + PARSE_TYPE + " TEXT, "
                + "FOREIGN KEY(" + RULE_ID + ") REFERENCES " + Rule.TABLE + "(" + Rule.RULE_ID + ")"
                + ");";
    }

    /**
     * ActionParam table.
     */
    public class ActionParam {
        public static final String TABLE = "action_param";

        // columns
        public static final String ID = "_id";
        public static final String ACTION_PARAM_ID = "action_param_id";
        public static final String ACTION_ID = "action_id";
        public static final String KEY = "key";
        public static final String VALUE = "value";

        // create table
        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ACTION_PARAM_ID + " INTEGER NOT NULL UNIQUE, "
                + ACTION_ID + " INTEGER NOT NULL, "
                + KEY + " TEXT NOT NULL, "
                + VALUE + " TEXT, "
                + "FOREIGN KEY(" + ACTION_ID + ") REFERENCES " + Action.TABLE + "(" + Action.ACTION_ID + ")"
                + ");";
    }

    /**
     * TransformerMapping table.
     */
    public class TransformerMapping {
        public static final String TABLE = "transformer";

        // columns
        public static final String ID = "_id";
        public static final String TRANSFORMER_ID = "transformer_id";
        public static final String RULE_ID = "rule_id";
        public static final String NAME = "name";
        public static final String PARSE_EXPRESSION = "parse_expression";
        public static final String PARSE_TYPE = "parse_type";

        // create table
        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TRANSFORMER_ID + " INTEGER NOT NULL UNIQUE, "
                + RULE_ID + " INTEGER NOT NULL, "
                + NAME + " TEXT NOT NULL, "
                + PARSE_EXPRESSION + " TEXT, "
                + PARSE_TYPE + " TEXT, "
                + "FOREIGN KEY(" + RULE_ID + ") REFERENCES " + Rule.TABLE + "(" + Rule.RULE_ID + ")"
                + ");";
    }

    /**
     * GradeEntry table.
     */
    public class GradeEntry {
        public static final String TABLE = "grade_entry";

        // columns
        public static final String ID = "_id";
        public static final String NAME = "name";
        public static final String GRADE = "grade";
        public static final String EXAM_ID = "exam_id";
        public static final String SEMESTER = "semester";
        public static final String STATE = "state";

        // create table
        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NAME + " TEXT NOT NULL, "
                + GRADE + " REAL, "
                + EXAM_ID + " TEXT, "
                + SEMESTER + " TEXT NOT NULL, "
                + STATE + " TEXT "
                + ");";
    }

    /**
     * Exams overview table.
     */
    public class Overview {
        public static final String TABLE = "overview";

        // columns
        public static final String ID = "_id";
        public static final String GRADE_ENTRY_ID = "grade_entry_id";
        public static final String AVERAGE = "average";
        public static final String PARTICIPANTS = "participants";
        public static final String SECTION_1 = "section_1";
        public static final String SECTION_2 = "section_2";
        public static final String SECTION_3 = "section_3";
        public static final String SECTION_4 = "section_4";
        public static final String SECTION_5 = "section_5";
        public static final String USER_SECTION = "user_section";

        // create table
        private static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GRADE_ENTRY_ID + " INTEGER NOT NULL UNIQUE, "
                + AVERAGE + " REAL, "
                + PARTICIPANTS + " INTEGER, "
                + SECTION_1 + " INTEGER, "
                + SECTION_2 + " INTEGER, "
                + SECTION_3 + " INTEGER, "
                + SECTION_4 + " INTEGER, "
                + SECTION_5 + " INTEGER, "
                + USER_SECTION + " INTEGER, "
                + "FOREIGN KEY(" + GRADE_ENTRY_ID + ") REFERENCES " + GradeEntry.TABLE + "(" + GradeEntry.ID + ")"
                + ");";
    }
}
