package de.mygrades.main.processor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.ConnectException;

import de.greenrobot.event.EventBus;
import de.mygrades.MyGradesApplication;
import de.mygrades.database.dao.DaoSession;
import de.mygrades.main.events.ErrorEvent;
import de.mygrades.main.rest.RestClient;
import retrofit.RetrofitError;

/**
 * Base class for all processors to hold relevant objects.
 */
public abstract class BaseProcessor {
    private static final String TAG = BaseProcessor.class.getSimpleName();

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

    /**
     * Post an ErrorEvent for retrofit errors.
     * @param TAG - TAG for logging
     * @param e RetrofitError
     */
    protected void postRetrofitError(String TAG, RetrofitError e) {
        if (e.getCause() instanceof ConnectException) {
            postErrorEvent(ErrorEvent.ErrorType.TIMEOUT, "Timeout", e);
        } else {
            postErrorEvent(ErrorEvent.ErrorType.GENERAL, "General Error", e);
        }
        Log.e(TAG, "RetrofitError: " + e.getMessage());
    }

    /**
     * Post an ErrorEvent on the Event Bus.
     * @param type type of the Error
     * @param msg Message of the error
     * @param e Exception which was raised
     */
    protected void postErrorEvent(ErrorEvent.ErrorType type, String msg, Exception e) {
        postErrorEvent(type, msg);

        Log.e(TAG, msg, e);
    }

    /**
     * Post an ErrorEvent on the Event Bus.
     * @param type type of the Error
     * @param msg Message of the error
     */
    protected void postErrorEvent(ErrorEvent.ErrorType type, String msg) {
        // post error event to subscribers
        ErrorEvent errorEvent = new ErrorEvent(type, msg);
        EventBus.getDefault().post(errorEvent);
    }
}
