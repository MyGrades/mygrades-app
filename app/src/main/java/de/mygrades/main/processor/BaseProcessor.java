package de.mygrades.main.processor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

    /**
     * Checks whether a Network interface is available and a connection is possible.
     * @return boolean
     */
    protected boolean isOnline() {
        // getActiveNetworkInfo() -> first connected network interface or null
        // getNetworkInfo(ConnectivityManager.TYPE_WIFI | TYPE_MOBILE) -> for wifi | mobile
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
