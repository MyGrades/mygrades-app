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
     * Start a worker thread to load all universities from the server.
     *
     * @param publishedOnly - only published universities or all.
     */
    public void getUniversities(boolean publishedOnly) {
        int method = MainService.METHOD_GET_UNIVERSITIES;

        // set request id
        long requestId = concatenateLong(method, 0);

        // start worker thread in background
        Intent intent = getBasicIntent(MainService.PROCESSOR_UNIVERSITY, method, requestId);
        intent.putExtra(MainService.PUBLISHED_ONLY, publishedOnly);
        context.startService(intent);
    }

    /**
     * Starts a worker thread to get a detailed university from the server.
     *
     * @param universityId - university id
     */
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
     * Load all universities from the database.
     *
     * @param publishedOnly - only published universities or all.
     */
    public void getUniversitiesFromDatabase(boolean publishedOnly) {
        int method = MainService.METHOD_GET_UNIVERSITIES_FROM_DATABASE;

        // set request id
        long requestId = concatenateLong(method, 0);

        // start worker thread in background
        Intent intent = getBasicIntent(MainService.PROCESSOR_UNIVERSITY, method, requestId);
        intent.putExtra(MainService.PUBLISHED_ONLY, publishedOnly);
        context.startService(intent);
    }

    /**
     * Starts a worker thread to scrape for new grades.
     */
    public void scrapeForGrades(boolean initialScraping) {
        int method = MainService.METHOD_SCRAPE_FOR_GRADES;

        // set request id
        // important: loginAndScrapeForGrades() must use the same requestId to avoid duplicate scraping
        long requestId = concatenateLong(method, 0);

        // start worker thread in background
        Intent intent = getBasicIntent(MainService.PROCESSOR_GRADES, method, requestId);
        intent.putExtra(MainService.INITIAL_SCRAPING, initialScraping);
        context.startService(intent);
    }

    /**
     * Starts a worker thread to scrape for the overview of a specific subject.
     * @param gradeHash - Hash of Grade/Subject (unique identifier)
     */
    public void scrapeForOverview(String gradeHash) {
        int method = MainService.METHOD_SCRAPE_FOR_OVERVIEW;

        // set request id
        long requestId = concatenateLong(method, gradeHash.hashCode());

        // start worker thread in background
        Intent intent = getBasicIntent(MainService.PROCESSOR_GRADES, method, requestId);
        intent.putExtra(MainService.GRADE_HASH, gradeHash);
        context.startService(intent);
    }

    /**
     * Load all information for Grade Detail from Database.
     * @param gradeHash - Hash of Grade/Subject (unique identifier)
     */
    public void getGradeDetails(String gradeHash) {
        int method = MainService.METHOD_GET_GRADE_DETAILS;

        // set request id
        long requestId = concatenateLong(method, 0);

        // start worker thread in background
        Intent intent = getBasicIntent(MainService.PROCESSOR_GRADES, method, requestId);
        intent.putExtra(MainService.GRADE_HASH, gradeHash);
        context.startService(intent);
    }

    /**
     * Starts a worker thread to save the username and password
     * and starts scraping for grades afterwards.
     *
     * @param username - username
     * @param password - password
     */
    public void loginAndScrapeForGrades(String username, String password, long universityId) {
        int method = MainService.METHOD_LOGIN_AND_SCRAPE_FOR_GRADES;

        // set request id
        // important: scrapeForGrades() must use the same requestId, to avoid duplicate scraping.
        long requestId = concatenateLong(MainService.METHOD_SCRAPE_FOR_GRADES, 0);

        // start worker thread in background
        Intent intent = getBasicIntent(MainService.PROCESSOR_LOGIN, method, requestId);
        intent.putExtra(MainService.USERNAME, username);
        intent.putExtra(MainService.PASSWORD, password);
        intent.putExtra(MainService.UNIVERSITY_ID, universityId);
        context.startService(intent);
    }

    /**
     * Starts a worker thread to get the username and selected university from database.
     */
    public void getLoginDataFromDatabase() {
        int method = MainService.METHOD_GET_LOGIN_DATA_FROM_DATABASE;

        // set request id
        long requestId = concatenateLong(method, 0);

        // start worker thread in background
        Intent intent = getBasicIntent(MainService.PROCESSOR_LOGIN, method, requestId);
        context.startService(intent);
    }

    /**
     * Starts a worker thread to delete all userdata and grades from the database.
     */
    public void logout() {
        int method = MainService.METHOD_LOGOUT;

        // set request id
        long requestId = concatenateLong(method, 0);

        // start worker thread in background
        Intent intent = getBasicIntent(MainService.PROCESSOR_LOGIN, method, requestId);
        context.startService(intent);
    }

    /**
     * Build a basic intent with required extra data for each request.
     *
     * @param processor - processor to create (declared in the MainService)
     * @param method - method to call (declared in the MainService)
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
     * This can be used to generate unique request ids.
     *
     * @param a first long value
     * @param b second long value
     * @return ab as long
     */
    private long concatenateLong(long a, long b) {
        return Long.parseLong("" + Math.abs(a) + Math.abs(b));
    }
}
