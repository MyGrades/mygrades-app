package de.mygrades.util;

/**
 * Global constants.
 */
public class Constants {

    // secure preferences constants
    public static final String NOT_SO_SECURE_PREF_FILE = "userdata";
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_PASSWORD = "password";

    // normal preferences constants
    public static final String PREF_KEY_UNIVERSITY_ID = "university_id";
    public static final String PREF_KEY_RULE_ID = "rule_id";
    public static final String PREF_KEY_INITIAL_LOADING_DONE = "initial_loading_done";
    public static final String PREF_KEY_LAST_UPDATED_AT = "last_updated_at";
    public static final String PREF_KEY_SCRAPING_FAILS_INTERVAL = "scraping_fails_interval";
    public static final String PREF_KEY_ALARM_ERROR_SCRAPING_COUNT = "alarm_error_scraping_count";
    public static final String PREF_KEY_DISMISSED_NOTIFICATION_INFO = "pref_key_dismissed_notification_info";

    // Scraping status instance state
    public static final String INSTANCE_IS_SCRAPING_STATE = "is_scraping_state";
    public static final String INSTANCE_PROGRESS_STATE = "progress_state";

    // grade entry 'seen' state
    public static final int GRADE_ENTRY_SEEN = 0;
    public static final int GRADE_ENTRY_NEW = 1;
    public static final int GRADE_ENTRY_UPDATED = 2;
}
