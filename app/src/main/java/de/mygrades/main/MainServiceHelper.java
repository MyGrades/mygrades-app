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

    public long getUniversities() {
        int method = MainService.METHOD_GET_UNIVERSITIES;

        // set request id and add it to the set
        long requestId = concatenateLong(method, 0);

        // get intent
        Intent intent = getBasicIntent(MainService.PROCESSOR_UNIVERSITY, method, requestId);

        // start worker thread in background
        context.startService(intent);

        return requestId;
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

    /**
     * Concatenates two long values.
     * This is used to generate unique request ids.
     *
     * @param a first long value
     * @param b second long value
     * @return ab as long
     */
    private long concatenateLong(long a, long b) {
        return Long.parseLong("" + a + b);
    }
}
