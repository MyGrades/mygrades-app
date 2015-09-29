package de.mygrades.main.processor;

import android.content.Context;

import de.mygrades.MyGradesApplication;
import de.mygrades.database.dao.DaoSession;
import de.mygrades.main.rest.RestClient;

/**
 * Base class for all processors to hold relevant objects.
 */
public abstract class BaseProcessor {
    protected Context context;
    protected RestClient restClient;
    protected DaoSession daoSession;

    public BaseProcessor(Context context) {
        this.context = context.getApplicationContext();
        this.restClient = new RestClient(this.context);
        this.daoSession = ((MyGradesApplication) this.context).getDaoSession();
    }
}
