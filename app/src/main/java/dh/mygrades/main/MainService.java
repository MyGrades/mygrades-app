package dh.mygrades.main;

import android.content.Intent;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import dh.mygrades.database.dao.GradeEntry;
import dh.mygrades.main.alarm.AlarmReceiver;
import dh.mygrades.main.processor.GradesProcessor;
import dh.mygrades.main.processor.LoginProcessor;
import dh.mygrades.main.processor.StatisticsProcessor;
import dh.mygrades.main.processor.UniversityProcessor;

/**
 * This service is used to handle ongoing operations in the background.
 * It is mostly used for network operations.
 *
 * The incoming intent must specify two extra integers (PROCESSOR_KEY, METHOD_KEY),
 * so the service can decide which processor to create and which method to call.
 */
public class MainService extends MultiThreadedIntentService {
    private static final String TAG = MainService.class.getSimpleName();

    // intent extra, processors: key and values
    public static final String PROCESSOR_KEY = "processor_key";
    public static final int PROCESSOR_UNIVERSITY = 100;
    public static final int PROCESSOR_GRADES = 101;
    public static final int PROCESSOR_LOGIN = 102;
    public static final int PROCESSOR_STATISTICS = 104;

    // intent extra, methods: key and values
    public static final String METHOD_KEY = "method_key";
    public static final int METHOD_GET_UNIVERSITIES = 111;
    public static final int METHOD_SCRAPE_FOR_GRADES = 112;
    public static final int METHOD_LOGIN_AND_SCRAPE_FOR_GRADES = 113;
    public static final int METHOD_GET_GRADES_FROM_DATABASE = 114;
    public static final int METHOD_GET_UNIVERSITIES_FROM_DATABASE = 115;
    public static final int METHOD_LOGOUT = 116;
    public static final int METHOD_GET_GRADE_DETAILS = 117;
    public static final int METHOD_SCRAPE_FOR_OVERVIEW = 118;
    public static final int METHOD_GET_LOGIN_DATA_FROM_DATABASE = 119;
    public static final int METHOD_GET_STATISTICS = 121;
    public static final int METHOD_POST_WISH = 122;
    public static final int METHOD_UPDATE_GRADE_ENTRY = 123;
    public static final int METHOD_UPDATE_GRADE_ENTRY_VISIBILITY = 124;
    public static final int METHOD_DELETE_GRADE_ENTRY = 125;

    // misc intent extra
    public static final String REQUEST_ID = "request_id";
    public static final String UNIVERSITY_ID = "university_id";
    public static final String RULE_ID = "rule_id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String PUBLISHED_ONLY = "published_only";
    public static final String GRADE_HASH = "grade_hash";
    public static final String INITIAL_SCRAPING = "initial_scraping";
    public static final String AUTOMATIC_SCRAPING = "automatic_scraping";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String UNIVERSITY_NAME = "university_name";
    public static final String WISH_MESSAGE = "wish_message";
    public static final String GRADE_ENTRY = "grade_entry";
    public static final String GRADE_ENTRY_HIDDEN = "grade_entry_hidden";

    // save request ids for pending request in this set, and remove them when its done.
    private Set<Long> pendingRequest;

    /**
     * Creates an IntentService. Invoked by your subclass's constructor.
     */
    public MainService() {
        pendingRequest = new HashSet<>();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        long requestId = intent.getLongExtra(REQUEST_ID, -1);
        if (pendingRequest.contains(requestId)) {
            // ignore intent if it is already pending
            return;
        }

        // add request id to pending requests
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
            case PROCESSOR_STATISTICS:
                handleStatisticsProcessor(method, intent);
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
     * @param method - the method to call, represented by an integer
     */
    private void handleUniversityProcessor(int method, Intent intent) {
            UniversityProcessor universityProcessor = new UniversityProcessor(this);

        switch(method) {
            case METHOD_GET_UNIVERSITIES:
                boolean publishedOnly = intent.getBooleanExtra(PUBLISHED_ONLY, false);
                universityProcessor.getUniversities(publishedOnly);
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
     * @param method - the method to call, represented by an integer
     */
    private void handleGradesProcessor(int method, Intent intent) {
        GradesProcessor gradesProcessor = new GradesProcessor(this);
        String gradeHash;

        switch (method) {
            case METHOD_SCRAPE_FOR_GRADES:
                boolean initialScraping = intent.getBooleanExtra(INITIAL_SCRAPING, false);
                boolean automaticScraping = intent.getBooleanExtra(AUTOMATIC_SCRAPING, false);
                gradesProcessor.scrapeForGrades(initialScraping, automaticScraping);
                // release wake lock if it got called automatically
                if (automaticScraping) {
                    Log.d(TAG, "wake lock released.");
                    AlarmReceiver.completeWakefulIntent(intent);
                }
                break;
            case METHOD_GET_GRADES_FROM_DATABASE:
                gradesProcessor.getGradesFromDatabase(false);
                break;
            case METHOD_GET_GRADE_DETAILS:
                gradeHash = intent.getStringExtra(GRADE_HASH);
                gradesProcessor.getGradeDetails(gradeHash);
                break;
            case METHOD_SCRAPE_FOR_OVERVIEW:
                gradeHash = intent.getStringExtra(GRADE_HASH);
                gradesProcessor.scrapeForOverview(gradeHash);
                break;
            case METHOD_UPDATE_GRADE_ENTRY:
                GradeEntry gradeEntry = intent.getParcelableExtra(GRADE_ENTRY);
                gradesProcessor.updateGradeEntry(gradeEntry);
                break;
            case METHOD_UPDATE_GRADE_ENTRY_VISIBILITY:
                gradeHash = intent.getStringExtra(GRADE_HASH);
                boolean hidden = intent.getBooleanExtra(GRADE_ENTRY_HIDDEN, false);
                gradesProcessor.updateGradeEntryVisibility(gradeHash, hidden);
                break;
            case METHOD_DELETE_GRADE_ENTRY:
                gradeHash = intent.getStringExtra(GRADE_HASH);
                gradesProcessor.deleteGradeEntry(gradeHash);
                break;
            default:
                Log.e(TAG, "Invalid method call to MainService: "+ method);
        }
    }

    /**
     * Decides which method to call from login processor.
     *
     * @param method - the method to call, represented by an integer
     * @param intent - intent
     */
    private void handleLoginProcessor(int method, Intent intent) {
        LoginProcessor loginProcessor = new LoginProcessor(this);

        switch (method) {
            case METHOD_LOGIN_AND_SCRAPE_FOR_GRADES:
                String username = intent.getStringExtra(USERNAME);
                String password = intent.getStringExtra(PASSWORD);
                long universityId = intent.getLongExtra(UNIVERSITY_ID, -1);
                long ruleId = intent.getLongExtra(RULE_ID, -1);

                loginProcessor.loginAndScrapeForGrades(username, password, universityId, ruleId);
                break;
            case METHOD_GET_LOGIN_DATA_FROM_DATABASE:
                loginProcessor.getLoginDataFromDatabase();
                break;
            case METHOD_LOGOUT:
                loginProcessor.logout();
                break;
            default:
                Log.e(TAG, "Invalid method call to MainService: "+ method);
        }
    }

    /**
     * Decides which method to call from statistics processor.
     *
     * @param method - the method to call, represented by an integer
     * @param intent - intent
     */
    private void handleStatisticsProcessor(int method, Intent intent) {
        StatisticsProcessor statisticsProcessor = new StatisticsProcessor(this);

        switch (method) {
            case METHOD_GET_STATISTICS:
                statisticsProcessor.getStatistics();
                break;
            default:
                Log.e(TAG, "Invalid method call to MainService: "+ method);
        }
    }
}
