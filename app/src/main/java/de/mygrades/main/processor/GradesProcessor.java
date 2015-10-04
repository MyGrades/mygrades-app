package de.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.mygrades.database.dao.Action;
import de.mygrades.database.dao.ActionDao;
import de.mygrades.database.dao.GradeEntry;
import de.mygrades.database.dao.GradeEntryDao;
import de.mygrades.database.dao.Overview;
import de.mygrades.database.dao.Rule;
import de.mygrades.database.dao.University;
import de.mygrades.database.dao.UniversityDao;
import de.mygrades.main.core.Parser;
import de.mygrades.main.core.Scraper;
import de.mygrades.main.core.Transformer;
import de.mygrades.main.events.ErrorEvent;
import de.mygrades.main.events.GradesEvent;
import de.mygrades.main.events.InitialScrapingDoneEvent;
import de.mygrades.main.events.ScrapeProgressEvent;
import de.mygrades.util.Constants;
import de.mygrades.util.exceptions.ParseException;

/**
 * GradesProcessor is responsible to scrape for grades
 * and to post GradeEntries to subscribers, e.g. Activities.
 */
public class GradesProcessor extends BaseProcessor {
    private static final String TAG = GradesProcessor.class.getSimpleName();

    private static final String ACTION_TYPE_TABLE_GRADES = "table_grades";
    private static final String ACTION_TYPE_TABLE_OVERVIEW = "table_overview";

    public GradesProcessor(Context context) {
        super(context);
    }

    /**
     * Scrape for grades and post OverviewEvent if scraping was successful.
     * Otherwise, an ErrorEvent will be posted.
     */
    public void scrapeForOverview(String gradeHash) {
        // No Connection -> event no Connection, abort
        if (!isOnline()) {
            postErrorEvent(ErrorEvent.ErrorType.NO_NETWORK, "No Internet Connection!");
            return;
        }

        // get shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // update and get university
        University university = updateAndGetUniversity(prefs);

        // get rule for user
        Rule rule = getUserRule(university);

        // get actions for scrape for overview
        List<Action> actions = daoSession.getActionDao().queryBuilder()
                .where(ActionDao.Properties.Type.notEq(ACTION_TYPE_TABLE_GRADES))
                .orderAsc(ActionDao.Properties.Position).list();

        // get GradeEntry from DB by hash
        GradeEntry gradeEntry = daoSession.getGradeEntryDao().queryBuilder()
                .where(GradeEntryDao.Properties.Hash.eq(gradeHash)).unique();
        Log.d(TAG, gradeEntry.toString());
        // TODO: iterate through actions and search for placeholders

        try {
            // init Parser, Scraper, Transformer
            Parser parser = new Parser(context);

            Scraper scraper = new Scraper(actions, parser);

            // start scraping
            String scrapingResult = scraper.scrape();

            // start transforming
            Transformer transformer = new Transformer(rule, scrapingResult, parser);
            Overview overview = transformer.transformOverview(gradeEntry.getGrade());

            Log.d(TAG, overview.toString());

        } catch (ParseException e) {
            postErrorEvent(ErrorEvent.ErrorType.GENERAL, "Parse Error", e);
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                postErrorEvent(ErrorEvent.ErrorType.TIMEOUT, "Timeout", e);
            } else {
                postErrorEvent(ErrorEvent.ErrorType.GENERAL, "General Error", e);
            }
        } catch (Exception e) {
            postErrorEvent(ErrorEvent.ErrorType.GENERAL, "General Error", e);
        }
    }

    /**
     * Scrape for grades and post and GradeEvent if scraping was successful.
     * Otherwise, an ErrorEvent will be posted.
     */
    public void scrapeForGrades(boolean initialScraping) {
        // No Connection -> event no Connection, abort
        if (!isOnline()) {
            postErrorEvent(ErrorEvent.ErrorType.NO_NETWORK, "No Internet Connection!");
            return;
        }

        // get shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // update and get university
        University university = updateAndGetUniversity(prefs);

        // get rule for user
        Rule rule = getUserRule(university);

        // get actions for scrape for overview
        List<Action> actions = daoSession.getActionDao().queryBuilder()
                .where(ActionDao.Properties.Type.notEq(ACTION_TYPE_TABLE_OVERVIEW))
                .orderAsc(ActionDao.Properties.Position).list();

        // post status event (0% done)
        EventBus.getDefault().post(new ScrapeProgressEvent(0, actions.size() + 1));

        try {
            String scrapingResult;
            List<GradeEntry> gradeEntries;

            // init Parser, Scraper, Transformer
            Parser parser = new Parser(context);
            Scraper scraper = new Scraper(actions, parser);

            // start scraping
            scrapingResult = scraper.scrape();

            // start transforming
            Transformer transformer = new Transformer(rule, scrapingResult, parser);
            gradeEntries = transformer.transform();

            // post status event (100% done)
            EventBus.getDefault().post(new ScrapeProgressEvent(actions.size() + 1, actions.size() + 1));

            Log.d(TAG, gradeEntries.toString());

            // save grade entries in database
            if (gradeEntries != null && gradeEntries.size() > 0) {
                daoSession.getGradeEntryDao().insertOrReplaceInTx(gradeEntries);
            }

            // save last_updated_at timestamp
            saveLastUpdatedAt(prefs);

            // post event with new grades to activity
            GradesEvent gradesEvent = new GradesEvent();
            gradesEvent.setGrades(gradeEntries);
            EventBus.getDefault().post(gradesEvent);

            // set initial loading to done and send event to activity
            if (initialScraping) {
                prefs.edit().putBoolean(Constants.PREF_KEY_INITIAL_LOADING_DONE, true).apply();
                EventBus.getDefault().post(new InitialScrapingDoneEvent());
            }
        } catch (ParseException e) {
            postErrorEvent(ErrorEvent.ErrorType.GENERAL, "Parse Error", e);
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                postErrorEvent(ErrorEvent.ErrorType.TIMEOUT, "Timeout", e);
            } else {
                postErrorEvent(ErrorEvent.ErrorType.GENERAL, "General Error", e);
            }
        } catch (Exception e) {
            postErrorEvent(ErrorEvent.ErrorType.GENERAL, "General Error", e);
        }
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

    /**
     * Get the rule from university for user.
     * @param university university object
     * @return selected rule
     */
    private Rule getUserRule(University university) {
        // get bachelor rule // TODO: read from preferences?
        Rule rule = null;
        for(Rule r : university.getRules()) {
            if (r.getType().equalsIgnoreCase("bachelor")) {
                rule = r;
                break;
            }
        }
        return rule;
    }

    /**
     * Update user university (from shared preferences) via rest and return it.
     * @param prefs - shared preferences
     * @return university object
     */
    private University updateAndGetUniversity(SharedPreferences prefs) {
        long universityId = prefs.getLong(Constants.PREF_KEY_UNIVERSITY_ID, -1);

        // update university and rules
        UniversityProcessor universityProcessor = new UniversityProcessor(context);
        universityProcessor.getDetailedUniversity(universityId);

        // load university from database
        return daoSession.getUniversityDao().queryBuilder().where(UniversityDao.Properties.UniversityId.eq(universityId)).unique();
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
}
