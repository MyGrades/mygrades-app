package de.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.securepreferences.SecurePreferences;

import java.util.List;

import de.greenrobot.event.EventBus;
import de.mygrades.database.dao.Rule;
import de.mygrades.database.dao.RuleDao;
import de.mygrades.database.dao.University;
import de.mygrades.database.dao.UniversityDao;
import de.mygrades.main.events.LoginDataEvent;
import de.mygrades.util.Constants;

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
    public void loginAndScrapeForGrades(String username, String password, long universityId) {
        // save selected universityId to shared preferences
        saveSelectedUniversity(universityId);

        // save login data to secure preferences
        saveLoginData(username, password);

        // start initial scraping
        GradesProcessor gradesProcessor = new GradesProcessor(context);
        gradesProcessor.scrapeForGrades(true);

        // TODO: post university id to our server (asynchronous with retrofit)
    }

    public void getLoginDataFromDatabase() {
        // get universityId from shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long universityId = prefs.getLong(Constants.PREF_KEY_UNIVERSITY_ID, -1);

        // get university name from database
        University university = daoSession.getUniversityDao().queryBuilder()
                  .where(UniversityDao.Properties.UniversityId.eq(universityId))
                  .unique();

        String universityName = university != null ? university.getName() : "";

        // get username from shared preferences
        SecurePreferences securePrefs = getSecurePreferences();
        String username = securePrefs.getString(Constants.PREF_KEY_USERNAME, "");

        LoginDataEvent loginDataEvent = new LoginDataEvent(username, universityId, universityName);
        EventBus.getDefault().post(loginDataEvent);
    }

    /**
     * Remove all userdata and grade entries from the database.
     */
    public void logout() {
        // remove username and password
        SharedPreferences securePrefs = getSecurePreferences();
        SecurePreferences.Editor secureEditor = (SecurePreferences.Editor) securePrefs.edit();
        secureEditor.remove(Constants.PREF_KEY_USERNAME);
        secureEditor.remove(Constants.PREF_KEY_PASSWORD);
        secureEditor.commit();

        // remove normal preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Constants.PREF_KEY_UNIVERSITY_ID);
        editor.remove(Constants.PREF_KEY_INITIAL_LOADING_DONE);
        editor.remove(Constants.PREF_KEY_LAST_UPDATED_AT);
        editor.remove(Constants.PREF_KEY_RULE_ID);
        editor.apply();

        // delete all grades
        daoSession.getGradeEntryDao().deleteAll();
    }

    /**
     * Saves the selected universityId in the default shared preferences.
     *
     * @param universityId - university id
     */
    private void saveSelectedUniversity(long universityId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        // get first rule and its id TODO: pass selection through pattern
        List<Rule> rules = daoSession.getRuleDao().queryBuilder().where(RuleDao.Properties.UniversityId.eq(universityId)).build().list();
        long ruleId = rules.get(0).getRuleId();

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
        return new SecurePreferences(context, Constants.NOT_SO_SECURE_PREF_PW, Constants.NOT_SO_SECURE_PREF_FILE);
    }
}
