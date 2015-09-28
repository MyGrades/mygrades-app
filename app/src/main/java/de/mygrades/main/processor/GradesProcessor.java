package de.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.mygrades.database.dao.GradeEntry;
import de.mygrades.database.dao.Rule;
import de.mygrades.database.dao.University;
import de.mygrades.database.dao.UniversityDao;
import de.mygrades.main.core.Parser;
import de.mygrades.main.core.Scraper;
import de.mygrades.main.core.Transformer;
import de.mygrades.main.events.ErrorEvent;
import de.mygrades.main.events.GradesEvent;
import de.mygrades.util.Constants;
import de.mygrades.util.exceptions.ParseException;

/**
 * Created by Jonas on 20.09.2015.
 */
public class GradesProcessor extends BaseProcessor {
    private static final String TAG = GradesProcessor.class.getSimpleName();

    public GradesProcessor(Context context) {
        super(context);
    }


    public void scrapeForGrades() {
        // No Connection -> event no Connection, abort
        if (!isOnline()) {
            // post error event to subscribers
            ErrorEvent errorEvent = new ErrorEvent(ErrorEvent.ErrorType.NO_NETWORK, "No Internet Connection!");
            EventBus.getDefault().post(errorEvent);
            return;
        }

        // get university
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long universityId = prefs.getLong(Constants.PREF_KEY_UNIVERSITY_ID, -1);
        University university = daoSession.getUniversityDao().queryBuilder().where(UniversityDao.Properties.UniversityId.eq(universityId)).unique();

        // get bachelor rule // TODO: read from preferences?
        Rule rule = null;
        for(Rule r : university.getRules()) {
            if (r.getType().equalsIgnoreCase("bachelor")) {
                rule = r;
                break;
            }
        }

        try {
            String scrapingResult;
            List<GradeEntry> gradeEntries;

            // init Parser, Scraper, Transformer
            Parser parser = new Parser(context);
            Scraper scraper = new Scraper(rule.getActions(), parser);

            // start scraping
            scrapingResult = scraper.scrape();

            // start transforming
            Transformer transformer = new Transformer(rule, scrapingResult, parser);
            gradeEntries = transformer.transform();

            Log.d(TAG, gradeEntries.toString());

            // save grade entries in database
            if (gradeEntries != null && gradeEntries.size() > 0) {
                daoSession.getGradeEntryDao().insertOrReplaceInTx(gradeEntries);
            }

            // save last_updated_at timestamp
            saveLastUpdatedAt(prefs);

            // send event with new grades to activity
            GradesEvent gradesEvent = new GradesEvent();
            gradesEvent.setGrades(gradeEntries);
            EventBus.getDefault().post(gradesEvent);
        } catch (ParseException e) {
            // post error event to subscribers
            ErrorEvent errorEvent = new ErrorEvent(ErrorEvent.ErrorType.GENERAL, "Parser Error");
            EventBus.getDefault().post(errorEvent);

            Log.e(TAG, "Parser Error", e);
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                // post error event to subscribers
                ErrorEvent errorEvent = new ErrorEvent(ErrorEvent.ErrorType.TIMEOUT, "Timeout");
                EventBus.getDefault().post(errorEvent);
            } else {
                // post error event to subscribers
                ErrorEvent errorEvent = new ErrorEvent(ErrorEvent.ErrorType.GENERAL, "General Error");
                EventBus.getDefault().post(errorEvent);
            }

            Log.e(TAG, "Scrape Error", e);
        } catch (Exception e) {
            // post error event to subscribers
            ErrorEvent errorEvent = new ErrorEvent(ErrorEvent.ErrorType.GENERAL, "General Error");
            EventBus.getDefault().post(errorEvent);

            Log.e(TAG, "General Error", e);
        }
    }

    /**
     * Saves the current timestamp in shared preferences.
     *
     * @param prefs - shared preferences
     */
    private void saveLastUpdatedAt(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        long timestamp = System.currentTimeMillis(); // get utc timestamp
        editor.putLong(Constants.PREF_KEY_LAST_UPDATED_AT, timestamp);
        editor.apply();
    }

    /**
     * Checks whether a Network interface is available and a connection is possible.
     * @return boolean
     */
    private boolean isOnline() {
        // getActiveNetworkInfo() -> first connected network interface or null
        // getNetworkInfo(ConnectivityManager.TYPE_WIFI | TYPE_MOBILE) -> for wifi | mobile
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Load a grades from the database and post an event with all grades.
     */
    public void getGradesFromDatabase() {
        List<GradeEntry> gradeEntries = daoSession.getGradeEntryDao().loadAll();

        // post event with new grades to subscribers
        GradesEvent gradesEvent = new GradesEvent();
        gradesEvent.setGrades(gradeEntries);
        EventBus.getDefault().post(gradesEvent);
    }
}
