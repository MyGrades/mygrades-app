package de.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;

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
        gradesProcessor.scrapeForGrades();

        // TODO: post university id to our server (asynchronous with retrofit)
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
