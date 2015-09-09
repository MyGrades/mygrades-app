package de.mygrades.main.processor;

import android.content.ContentResolver;
import android.content.Context;

import de.mygrades.database.DBHelper;
import de.mygrades.main.rest.RestClient;

/**
 * Base class for all processors to hold relevant objects.
 */
public class BaseProcessor {
    private Context context;
    protected RestClient restClient;
    protected ContentResolver contentResolver;
    protected DBHelper dbHelper;

    public BaseProcessor(Context context) {
        this.context = context.getApplicationContext();
        this.restClient = new RestClient(this.context);
        this.contentResolver = this.context.getContentResolver();
        this.dbHelper = new DBHelper(this.context);
    }
}
