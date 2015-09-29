package de.mygrades.main;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import de.mygrades.main.processor.GradesProcessor;
import de.mygrades.main.processor.LoginProcessor;
import de.mygrades.main.processor.UniversityProcessor;

/**
 * This service is used to handle ongoing operations in the background.
 * It is mostly used for network operations.
 *
 * The incoming intent must specify two extra Strings (PROCESSOR_KEY, METHOD_KEY),
 * so the service can decide which processor to create and which method to call.
 */
public class MainService extends IntentService {
    private static final String TAG = MainService.class.getSimpleName();

    // intent extra, processors: key and values
    public static final String PROCESSOR_KEY = "processor_key";
    public static final int PROCESSOR_UNIVERSITY = 100;
    public static final int PROCESSOR_GRADES = 101;
    public static final int PROCESSOR_LOGIN = 102;

    // intent extra, methods: key and values
    public static final String METHOD_KEY = "method_key";
    public static final int METHOD_GET_UNIVERSITIES = 111;
    public static final int METHOD_GET_DETAILED_UNIVERSITY = 112;
    public static final int METHOD_SCRAPE_FOR_GRADES = 113;
    public static final int METHOD_LOGIN_AND_SCRAPE_FOR_GRADES = 114;
    public static final int METHOD_GET_GRADES_FROM_DATABASE = 115;
    public static final int METHOD_GET_UNIVERSITIES_FROM_DATABASE = 116;
    public static final int METHOD_LOGOUT = 117;

    // misc intent extra
    public static final String REQUEST_ID = "request_id";
    public static final String UNIVERSITY_ID = "university_id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String PUBLISHED_ONLY = "published_only";

    // save request ids for pending request in this set, and remove them when its done.
    private Set<Long> pendingRequest;

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     */
    public MainService() {
        super(TAG);
        pendingRequest = new HashSet<>();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        long requestId = intent.getLongExtra(REQUEST_ID, -1);
        if (pendingRequest.contains(requestId)) {
            // ignore intent if it is already pending
            return;
        }

        // add request to pending requests
        pendingRequest.add(requestId);

        // add intent to message queue
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // get desired processor and method from extras
        int processor = intent.getIntExtra(PROCESSOR_KEY, -1);
        int method = intent.getIntExtra(METHOD_KEY, -1);

        switch (processor) {
            case PROCESSOR_UNIVERSITY:
                handleUniversityProcessor(method, intent);
                break;
            case PROCESSOR_GRADES:
                handleGradesProcessor(method, intent);
                break;
            case PROCESSOR_LOGIN:
                handleLoginProcessor(method, intent);
                break;
            default:
                Log.e(TAG, "Invalid processor call to MainService: " + processor);
                break;
        }

        // remove request id from pending requests
        long requestId = intent.getLongExtra(REQUEST_ID, -1);
        pendingRequest.remove(requestId);
    }

    /**
     * Decides which method to call from the university processor.
     *
     * @param method - the method to call, indicated with an integer
     */
    private void handleUniversityProcessor(int method, Intent intent) {
        UniversityProcessor universityProcessor = new UniversityProcessor(this);

        switch(method) {
            case METHOD_GET_UNIVERSITIES:
                boolean publishedOnly = intent.getBooleanExtra(PUBLISHED_ONLY, false);
                universityProcessor.getUniversities(publishedOnly);
                break;
            case METHOD_GET_DETAILED_UNIVERSITY:
                long universityId = intent.getLongExtra(UNIVERSITY_ID, 0);
                universityProcessor.getDetailedUniversity(universityId);
                break;
            case METHOD_GET_UNIVERSITIES_FROM_DATABASE:
                publishedOnly = intent.getBooleanExtra(PUBLISHED_ONLY, false);
                universityProcessor.getUniversitiesFromDatabase(publishedOnly);
                break;
            default:
                Log.e(TAG, "Invalid method call to MainService: "+ method);
        }
    }

    /**
     * Decides which method to call from the grades processor.
     *
     * @param method - the method to call, indicated with an integer
     * @param intent - intent
     */
    private void handleGradesProcessor(int method, Intent intent) {
        GradesProcessor gradesProcessor = new GradesProcessor(this);

        switch (method) {
            case METHOD_SCRAPE_FOR_GRADES:
                gradesProcessor.scrapeForGrades();
                break;
            case METHOD_GET_GRADES_FROM_DATABASE:
                gradesProcessor.getGradesFromDatabase();
                break;
            default:
                Log.e(TAG, "Invalid method call to MainService: "+ method);
        }
    }

    /**
     * Decides which method to call from login processor.
     *
     * @param method - the method to call, indicated with an integer
     * @param intent - intent
     */
    private void handleLoginProcessor(int method, Intent intent) {
        LoginProcessor loginProcessor = new LoginProcessor(this);

        switch (method) {
            case METHOD_LOGIN_AND_SCRAPE_FOR_GRADES:
                String username = intent.getStringExtra(USERNAME);
                String password = intent.getStringExtra(PASSWORD);
                loginProcessor.loginAndScrapeForGrades(username, password);
                break;
            case METHOD_LOGOUT:
                loginProcessor.logout();
                break;
            default:
                Log.e(TAG, "Invalid method call to MainService: "+ method);
        }
    }
}
