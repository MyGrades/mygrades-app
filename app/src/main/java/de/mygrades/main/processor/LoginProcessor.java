package de.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.securepreferences.SecurePreferences;

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
     * Saves the username and password and starts the scraping afterwards.
     *
     * @param username - username
     * @param password - password
     */
    public void loginAndScrapeForGrades(String username, String password) {
        // save login data to secure preferences
        saveLoginData(username, password);

        // start scraping
        GradesProcessor gradesProcessor = new GradesProcessor(context);
        gradesProcessor.scrapeForGrades(true);

        // TODO: post university id to our server (asynchronous with retrofit)
    }

    /**
     * Remove all userdata and grade entries from the database.
     */
    public void logout() {
        // remove username and password
        SharedPreferences securePrefs = new SecurePreferences(context, Constants.NOT_SO_SECURE_PREF_PW, Constants.NOT_SO_SECURE_PREF_FILE);
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
        editor.commit();

        // delete all grades
        daoSession.getGradeEntryDao().deleteAll();
    }

    /**
     * Saves username and password in secure preferences.
     */
    private void saveLoginData(String username, String password) {
        SharedPreferences prefs = new SecurePreferences(context, Constants.NOT_SO_SECURE_PREF_PW, Constants.NOT_SO_SECURE_PREF_FILE);
        SecurePreferences.Editor editor = (SecurePreferences.Editor) prefs.edit();
        editor.putString(Constants.PREF_KEY_USERNAME, username);
        editor.putString(Constants.PREF_KEY_PASSWORD, password);
        editor.commit();
    }
}
