package dh.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.securepreferences.SecurePreferences;

import de.greenrobot.event.EventBus;
import dh.mygrades.R;
import dh.mygrades.database.dao.University;
import dh.mygrades.database.dao.UniversityDao;
import dh.mygrades.main.alarm.ScrapeAlarmManager;
import dh.mygrades.main.events.LoginDataEvent;
import dh.mygrades.util.Config;
import dh.mygrades.util.Constants;

/**
 * LoginProcessor is responsible to securely save the username and password
 * and start the scraping afterwards.
 */
public class LoginProcessor extends BaseProcessor {
    public LoginProcessor(Context context) {
        super(context);
    }

    /**
     * Saves the username and password to the secure preferences,
     * the selected universityId to the shared preferences.
     *
     * Afterwards the initial scraping will be started.
     *
     * @param username - username
     * @param password - password
     * @param universityId - university id
     */
    public void loginAndScrapeForGrades(String username, String password, long universityId, long ruleId) {
        // save selected universityId to shared preferences
        saveSelectedUniversity(universityId, ruleId);

        // save login data to secure preferences
        saveLoginData(username, password);

        // get login data from database to post LoginDataEvent
        getLoginDataFromDatabase();

        // start initial scraping
        GradesProcessor gradesProcessor = new GradesProcessor(context);
        gradesProcessor.scrapeForGrades(true);
    }

    public void getLoginDataFromDatabase() {
        // get universityId from shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long universityId = prefs.getLong(Constants.PREF_KEY_UNIVERSITY_ID, -1);
        long ruleId = prefs.getLong(Constants.PREF_KEY_RULE_ID, -1);

        // get university name from database
        University university = daoSession.getUniversityDao().queryBuilder()
                  .where(UniversityDao.Properties.UniversityId.eq(universityId))
                  .unique();

        String universityName = university != null ? university.getName() : "";

        // get username from shared preferences
        SecurePreferences securePrefs = getSecurePreferences();
        String username = securePrefs.getString(Constants.PREF_KEY_USERNAME, "");

        LoginDataEvent loginDataEvent = new LoginDataEvent(username, universityId, ruleId, universityName);
        EventBus.getDefault().post(loginDataEvent);
    }

    /**
     * Remove all userdata from preferences and clear database tables.
     */
    public void logout() {
        deletePreferences();

        // delete all grades
        daoSession.getGradeEntryDao().deleteAll();

        // delete all actions and transformer mappings
        daoSession.getTransformerMappingDao().deleteAll();
        daoSession.getActionParamDao().deleteAll();
        daoSession.getActionDao().deleteAll();

        // assure that the session is cleared
        daoSession.clear();
    }

    /**
     * Deletes all normal and secure preferences and restores default values afterwards.
     */
    public void deletePreferences() {
        // remove username and password
        SharedPreferences securePrefs = getSecurePreferences();
        SecurePreferences.Editor secureEditor = (SecurePreferences.Editor) securePrefs.edit();
        secureEditor.remove(Constants.PREF_KEY_USERNAME);
        secureEditor.remove(Constants.PREF_KEY_PASSWORD);
        secureEditor.commit();

        // clear normal preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // restore default preferences
        PreferenceManager.setDefaultValues(context, R.xml.settings, true);

        // cancel possible active alarms
        ScrapeAlarmManager scrapeAlarmManager = new ScrapeAlarmManager(context);
        scrapeAlarmManager.cancelAlarm();
    }

    /**
     * Saves the selected universityId in the default shared preferences.
     *
     * @param universityId - university id
     */
    private void saveSelectedUniversity(long universityId, long ruleId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(Constants.PREF_KEY_UNIVERSITY_ID, universityId);
        editor.putLong(Constants.PREF_KEY_RULE_ID, ruleId);
        editor.apply();
    }

    /**
     * Saves username and password in secure preferences.
     */
    private void saveLoginData(String username, String password) {
        SharedPreferences prefs = getSecurePreferences();
        SecurePreferences.Editor editor = (SecurePreferences.Editor) prefs.edit();
        editor.putString(Constants.PREF_KEY_USERNAME, username);
        editor.putString(Constants.PREF_KEY_PASSWORD, password);
        editor.apply();
    }

    private SecurePreferences getSecurePreferences() {
        return new SecurePreferences(context, Config.getSecurePreferencesKey(), Constants.NOT_SO_SECURE_PREF_FILE);
    }
}
