package de.mygrades.main;

import android.content.Context;
import android.content.Intent;

/**
 * Helper class with convenience methods to start background
 * threads by sending intents to the MainService.
 *
 * This class is responsible for creating unique request ids for different requests.
 */
public class MainServiceHelper {
    private Context context;

    public MainServiceHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Build a basic intent with required extra data for each request.
     *
     * @param processor - processor to create (declared in the MainService)
     * @param method - method to call by (declared in the MainService)
     * @param requestId - request id
     * @return intent
     */
    private Intent getBasicIntent(int processor, int method, long requestId) {
        Intent intent = new Intent(context, MainService.class);
        intent.putExtra(MainService.PROCESSOR_KEY, processor);
        intent.putExtra(MainService.METHOD_KEY, method);
        intent.putExtra(MainService.REQUEST_ID, requestId);
        return intent;
    }
}
