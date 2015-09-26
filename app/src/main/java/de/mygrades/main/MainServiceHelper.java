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

    public void getUniversities(boolean publishedOnly) {
        int method = MainService.METHOD_GET_UNIVERSITIES;

        // set request id
        long requestId = concatenateLong(method, 0);

        // start worker thread in background
        Intent intent = getBasicIntent(MainService.PROCESSOR_UNIVERSITY, method, requestId);
        intent.putExtra(MainService.PUBLISHED_ONLY, publishedOnly);
        context.startService(intent);
    }

    public void getDetailedUniversity(long universityId) {
        int method = MainService.METHOD_GET_DETAILED_UNIVERSITY;

        // set request id
        long requestId = concatenateLong(method, universityId);

        // start worker thread in background
        Intent intent = getBasicIntent(MainService.PROCESSOR_UNIVERSITY, method, requestId);
        intent.putExtra(MainService.UNIVERSITY_ID, universityId);
        context.startService(intent);
    }

    /**
     * Load all grades from the database.
     */
    public void getGradesFromDatabase() {
        int method = MainService.METHOD_GET_GRADES_FROM_DATABASE;

        // set request id
        long requestId = concatenateLong(method, 0);

        // start worker thread in background
        Intent intent = getBasicIntent(MainService.PROCESSOR_GRADES, method, requestId);
        context.startService(intent);
    }

    /**
     * Starts an IntentService to scrape for new grades.
     */
    public void scrapeForGrades() {
        int method = MainService.METHOD_SCRAPE_FOR_GRADES;

        // set request id
        long requestId = concatenateLong(method, 0);

        // start worker thread in background
        Intent intent = getBasicIntent(MainService.PROCESSOR_GRADES, method, requestId);
        context.startService(intent);
    }

    /**
     * Starts an IntentService to save the username and password
     * and starts scraping for grades afterwards.
     *
     * @param username - username
     * @param password - password
     */
    public void loginAndScrapeForGrades(String username, String password) {
        int method = MainService.METHOD_LOGIN_AND_SCRAPE_FOR_GRADES;

        // set request id
        long requestId = concatenateLong(method, 0);

        // start worker thread in background
        Intent intent = getBasicIntent(MainService.PROCESSOR_LOGIN, method, requestId);
        intent.putExtra(MainService.USERNAME, username);
        intent.putExtra(MainService.PASSWORD, password);
        context.startService(intent);
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
