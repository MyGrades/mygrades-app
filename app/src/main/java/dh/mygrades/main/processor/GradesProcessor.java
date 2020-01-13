package dh.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;
import dh.mygrades.database.dao.Action;
import dh.mygrades.database.dao.ActionDao;
import dh.mygrades.database.dao.GradeEntry;
import dh.mygrades.database.dao.GradeEntryDao;
import dh.mygrades.database.dao.Overview;
import dh.mygrades.database.dao.Rule;
import dh.mygrades.database.dao.RuleDao;
import dh.mygrades.main.alarm.ScrapeAlarmManager;
import dh.mygrades.main.core.Parser;
import dh.mygrades.main.core.Scraper;
import dh.mygrades.main.core.Transformer;
import dh.mygrades.main.events.DeleteGradeEvent;
import dh.mygrades.main.events.ErrorEvent;
import dh.mygrades.main.events.GradeEntryEvent;
import dh.mygrades.main.events.GradesEvent;
import dh.mygrades.main.events.InitialScrapingDoneEvent;
import dh.mygrades.main.events.IntermediateTableScrapingResultEvent;
import dh.mygrades.main.events.OverviewEvent;
import dh.mygrades.main.events.OverviewPossibleEvent;
import dh.mygrades.main.events.ScrapeProgressEvent;
import dh.mygrades.util.Constants;
import dh.mygrades.util.SemesterMapper;
import dh.mygrades.util.exceptions.ParseException;

/**
 * GradesProcessor is responsible to scrape for grades
 * and to post GradeEntries to subscribers, e.g. Activities.
 */
public class GradesProcessor extends BaseProcessor {
    private static final String TAG = GradesProcessor.class.getSimpleName();

    public static final String RULE_TYPE_MULTIPLE_TABLES = "multiple_tables";
    public static final String ACTION_TYPE_TABLE_GRADES = "table_grades";
    public static final String ACTION_TYPE_TABLE_OVERVIEW = "table_overview";
    public static final String ACTION_TYPE_TABLE_GRADES_ITERATOR = "table_grades_iterator";

    private String gradeHash;
    private SemesterMapper semesterMapper;

    public GradesProcessor(Context context) {
        super(context);
        semesterMapper = new SemesterMapper();
    }

    /**
     * Get needed information for grade detail with overview.
     * Retrieves GradeEntry from DB and posts Event.
     *
     * @param gradeHash identify requested gradeEntry
     */
    public void getGradeDetails(String gradeHash) {
        // get GradeEntry from DB by hash with Overview (if present)
        GradeEntry gradeEntry = daoSession.getGradeEntryDao().load(gradeHash);

        // get semester mapping (used in edit mode)
        List<GradeEntry> gradeEntries = daoSession.getGradeEntryDao().loadAll();
        SemesterMapper semesterMapper = new SemesterMapper();
        Map<String, Integer> semesterToNumberMap = semesterMapper.getSemesterToNumberMap(gradeEntries);

        if (gradeEntry == null) { // create new grade with generated hash
            gradeEntry = getGeneratedGradeEntry(semesterMapper.getSortedSemester());
        } else {
            daoSession.getGradeEntryDao().refresh(gradeEntry);

            // post sticky event to overview if seen state has changed
            if (gradeEntry.getSeen() != Constants.GRADE_ENTRY_SEEN) {
                gradeEntry.setSeen(Constants.GRADE_ENTRY_SEEN);
                daoSession.getGradeEntryDao().update(gradeEntry);
                EventBus.getDefault().postSticky(new GradesEvent(gradeEntry));
            }
        }

        Log.d(TAG, gradeEntry.toString());

        // post Event with GradeEntry to GUI
        GradeEntryEvent gradeEntryEvent = new GradeEntryEvent(gradeEntry);
        gradeEntryEvent.setSemesterToSemesterNumberMap(semesterToNumberMap);
        EventBus.getDefault().post(gradeEntryEvent);

        // if there is an overview for this grade -> post event to gui
        if (gradeEntry.getOverview() != null) {
            // post Event with Overview to GUI
            EventBus.getDefault().post(new OverviewEvent(gradeEntry.getOverview()));
        } else {
            // otherwise check if an overview is possible for user's university rule
            // get shared preferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            // get rule for user
            Rule rule = getUserRule(prefs);

            // send event to GUI whether overview is possible and it has not failed before
            Boolean overviewFailedOnFirstTry = gradeEntry.getOverviewFailedOnFirstTry();
            overviewFailedOnFirstTry = overviewFailedOnFirstTry == null ? false : overviewFailedOnFirstTry;
            if (!overviewFailedOnFirstTry) {
                EventBus.getDefault().post(new OverviewPossibleEvent(gradeEntry.getOverviewPossible(), rule.getOverview()));
            }
        }
    }

    /**
     * Creates a grade entry with a generated id and default values.
     * This is used for grade entries which are created by the user.
     *
     * @return grade entry
     */
    private GradeEntry getGeneratedGradeEntry(List<String> sortedSemester) {
        GradeEntry gradeEntry = new GradeEntry();
        gradeEntry.__setDaoSession(daoSession);
        gradeEntry.setOverviewPossible(false);
        gradeEntry.setWeight(1.0);
        gradeEntry.setName("Modulname");
        gradeEntry.setGeneratedId(UUID.randomUUID().toString());
        gradeEntry.setSeen(Constants.GRADE_ENTRY_SEEN);
        gradeEntry.setAttempt("1");
        gradeEntry.updateHash();
        gradeEntry.setModifiedSemester(sortedSemester.get(sortedSemester.size() - 1));
        return gradeEntry;
    }

    /**
     * Scrapes for Overview (and indirectly also grades) and posts a
     * OverviewEvent if scraping was successful.
     *
     * @param gradeHash identify requested gradeEntry
     */
    public void scrapeForOverview(String gradeHash) {
        this.gradeHash = gradeHash;

        // otherwise start scraping
        // No Connection -> event no Connection, abort
        if (!isOnline()) {
            postErrorEvent(ErrorEvent.ErrorType.NO_NETWORK, "No Internet Connection!");
            return;
        }

        // get GradeEntry from DB by hash with Overview (if present)
        final GradeEntry gradeEntry = daoSession.getGradeEntryDao().queryDeep("WHERE "+ GradeEntryDao.Properties.Hash.columnName +" = ?", gradeHash).get(0); // TODO: Nullpointer possible?
        Log.d(TAG, gradeEntry.toString());

        // get shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // update and get university
        updateUniversity(prefs);

        // get rule for user
        Rule rule = getUserRule(prefs);

        // register event bus -> listen for IntermediateTableScrapingResultEvent
        EventBus.getDefault().register(this);

        // make sure that actions get loaded from DB not cached ones
        daoSession.clear();
        // get actions for scrape for overview
        List<Action> actions = daoSession.getActionDao().queryBuilder()
                //.where(ActionDao.Properties.Type.notEq(ACTION_TYPE_TABLE_GRADES))
                .where(ActionDao.Properties.RuleId.eq(rule.getRuleId()))
                .orderAsc(ActionDao.Properties.Position).list();

        // replace placeholders in actions parseExpressions
        replacePlaceholdersInActions(gradeEntry, actions);

        try {
            // init Parser, Scraper, Transformer
            Parser parser = new Parser(context);

            Scraper scraper = new Scraper(actions, parser, gradeHash);

            // start scraping
            String scrapingResult = scraper.scrape(true);

            // start transforming
            Transformer transformer = new Transformer(rule, scrapingResult, parser);
            final Overview newOverview = transformer.transformOverview(gradeEntry.getGrade());
            newOverview.setGradeEntryHash(gradeEntry.getHash());

            // post status event (100% done)
            EventBus.getDefault().post(new ScrapeProgressEvent(actions.size() + 1, actions.size() + 1, true, gradeHash));

            // if old overview
            Overview existingOverview = gradeEntry.getOverview();
            if (existingOverview != null) {
                //if overview equals old one -> do nothing. if different -> update with values
                if (!existingOverview.equals(newOverview)) {
                    existingOverview.updateOverviewFromOther(newOverview);
                    daoSession.getOverviewDao().update(existingOverview);
                }
                // post Event with Overview to GUI
                EventBus.getDefault().post(new OverviewEvent(existingOverview, true));
            } else {
                // save new overview in database and update gradeEntry
                daoSession.runInTx(new Runnable() {
                    @Override
                    public void run() {
                        long overviewId = daoSession.getOverviewDao().insertOrReplace(newOverview);
                        gradeEntry.setOverviewId(overviewId);
                        daoSession.getGradeEntryDao().update(gradeEntry);
                    }
                });
                // post Event with Overview to GUI
                EventBus.getDefault().post(new OverviewEvent(newOverview, true));
            }
        } catch (ParseException e) {
            setOverviewFailedOnFirstTry(gradeEntry);
            postErrorEvent(ErrorEvent.ErrorType.GENERAL, "Parse Error", e);
        } catch (IOException e) {
            setOverviewFailedOnFirstTry(gradeEntry);
            if (e instanceof SocketTimeoutException) {
                postErrorEvent(ErrorEvent.ErrorType.TIMEOUT, "Timeout", e);
            } else {
                postErrorEvent(ErrorEvent.ErrorType.GENERAL, "General Error", e);
            }
        } catch (Exception e) {
            setOverviewFailedOnFirstTry(gradeEntry);
            postErrorEvent(ErrorEvent.ErrorType.GENERAL, "General Error", e);
        } finally {
            // unregister EventBus
            EventBus.getDefault().unregister(this);
        }
    }

    /**
     * Set overviewFailedOnFirstTry to true, so the scraping does not start again automatically.
     *
     * @param gradeEntry - GradeEntry
     */
    private void setOverviewFailedOnFirstTry(GradeEntry gradeEntry) {
        gradeEntry.setOverviewFailedOnFirstTry(true);
        gradeEntry.update();
    }

    /**
     * Scrape for grades and post and GradeEvent if scraping was successful.
     * Otherwise, an ErrorEvent will be posted.
     * @param initialScraping is this the inital scraping?
     */
    public void scrapeForGrades(boolean initialScraping) {
        scrapeForGrades(initialScraping, false);
    }

    /**
     * Scrape for grades and post and GradeEvent if scraping was successful.
     * Otherwise, an ErrorEvent will be posted.
     * @param initialScraping is this the inital scraping?
     * @param automaticScraping is scraping called automatically?
     */
    public void scrapeForGrades(boolean initialScraping, boolean automaticScraping) {
        // No Connection -> event no Connection, abort
        if (!isOnline()) {
            postErrorEvent(ErrorEvent.ErrorType.NO_NETWORK, "No Internet Connection!", automaticScraping);
            return;
        }

        // get shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // update and get university
        updateUniversity(prefs);

        // get rule for user
        Rule rule = getUserRule(prefs);

        // get actions for scrape for overview
        List<Action> actions = daoSession.getActionDao().queryBuilder()
                .where(ActionDao.Properties.Type.notEq(ACTION_TYPE_TABLE_OVERVIEW))
                .where(ActionDao.Properties.RuleId.eq(rule.getRuleId()))
                .orderAsc(ActionDao.Properties.Position).list();

        // post status event (0% done)
        EventBus.getDefault().post(new ScrapeProgressEvent(0, actions.size() + 1));

        try {
            List<GradeEntry> gradeEntries;

            // init Parser, Scraper
            Parser parser = new Parser(context);

            Scraper scraper = new Scraper(actions, parser);

            if (rule.getType() != null && rule.getType().equals(RULE_TYPE_MULTIPLE_TABLES)) {
                Map<String, String> scrapingResult = scraper.scrapeMultipleTables(Transformer.getTransformermappingMap(rule.getTransformerMappings()));
                Log.d(TAG, scrapingResult.toString());

                // start transforming
                Transformer transformer = new Transformer(rule, null, parser); // note: the html to parse the grades is set by transformMultipleTables
                gradeEntries = transformer.transformMultipleTables(scrapingResult);
            } else {
                // start scraping
                String scrapingResult = scraper.scrape();

                // start transforming
                Transformer transformer = new Transformer(rule, scrapingResult, parser);
                gradeEntries = transformer.transform();
            }

            Log.d(TAG, gradeEntries.toString());

            // post status event (100% done)
            EventBus.getDefault().post(new ScrapeProgressEvent(actions.size() + 1, actions.size() + 1, false));

            // save grade entries in database
            saveGradeEntriesToDB(gradeEntries, initialScraping, automaticScraping);

            // save last_updated_at timestamp
            saveLastUpdatedAt(prefs);

            daoSession.clear();
            gradeEntries = daoSession.getGradeEntryDao().loadAll();
            Map<String, Integer> semesterNumberMap = getSemesterNumberMap(gradeEntries);
            String actualFirstSemester = getActualFirstSemester(gradeEntries, semesterNumberMap);

            // post event with all grades to activity
            GradesEvent gradesEvent = new GradesEvent(gradeEntries, true, semesterNumberMap, actualFirstSemester);
            EventBus.getDefault().post(gradesEvent);

            // set initial loading to done and send event to activity
            if (initialScraping) {
                prefs.edit().putBoolean(Constants.PREF_KEY_INITIAL_LOADING_DONE, true).apply();
                EventBus.getDefault().postSticky(new InitialScrapingDoneEvent());
            }

            // reset one time counter if scraping was successful
            if (automaticScraping) {
                ScrapeAlarmManager scrapeAlarmManager = new ScrapeAlarmManager(context);
                scrapeAlarmManager.resetOneTimeCounters();
            }
        } catch (ParseException e) {
            postErrorEvent(ErrorEvent.ErrorType.GENERAL, "Parse Error", e, automaticScraping);
        } catch (IOException e) {
            if (e instanceof SocketTimeoutException) {
                postErrorEvent(ErrorEvent.ErrorType.TIMEOUT, "Timeout", e, automaticScraping);
            } else {
                postErrorEvent(ErrorEvent.ErrorType.GENERAL, "General Error", e, automaticScraping);
            }
        } catch (Exception e) {
            postErrorEvent(ErrorEvent.ErrorType.GENERAL, "General Error", e, automaticScraping);
        }
    }

    /**
     * Updates or inserts a grade entry in the database. The session must be re-attached, because
     * the gradeEntry was un-parcelled before (without the session).
     *
     * @param gradeEntry - grade entry to update or insert if it does not exist
     */
    public void updateGradeEntry(GradeEntry gradeEntry) {
        // re-attach grade entry to dao session
        gradeEntry.__setDaoSession(daoSession);

        if (daoSession.getGradeEntryDao().load(gradeEntry.getHash()) == null) {
            daoSession.getGradeEntryDao().insert(gradeEntry);
        } else {
            gradeEntry.update();
        }

        // post sticky grades event
        getGradesFromDatabase(true);
    }

    /**
     * Updates the visibility of a grade entry and posts an GradesEvent.
     *
     * @param gradeHash - grade entry hash
     * @param hidden - hidden or not
     */
    public void updateGradeEntryVisibility(String gradeHash, boolean hidden) {
        // find grade entry by hash and update visibility
        GradeEntry gradeEntry = daoSession.getGradeEntryDao().load(gradeHash);
        gradeEntry.setHidden(hidden);
        gradeEntry.update();

        // post event to ui
        EventBus.getDefault().post(new GradesEvent(gradeEntry));
    }

    /**
     * Deletes a GradeEntry by a given grade entry hash.
     *
     * @param gradeHash grade entry hash
     */
    public void deleteGradeEntry(String gradeHash) {
        // find grade entry by hash
        GradeEntry gradeEntry = daoSession.getGradeEntryDao().load(gradeHash);
        if (gradeEntry != null) {
            gradeEntry.delete();

            // post event to ui
            EventBus.getDefault().postSticky(new DeleteGradeEvent(gradeEntry));
        }
    }

    /**
     * Saves given list of GradeEntries to database.
     * Only new and updated values are written to database.
     * 1. Create Map: GradeHash -> GradeEntry Object for new and old list (not that hard -> only copy of references not deep copy)
     * 2. Separate new Entries and updated entries
     * 3. Update updated Entries and insert new entries
     * @param newGradeEntries List of (new) GradeEntries parsed from Website.
     */
    private void saveGradeEntriesToDB(List<GradeEntry> newGradeEntries, boolean initialScraping, boolean automaticScraping) {
        if (newGradeEntries != null && newGradeEntries.size() > 0) {
            Map<String, GradeEntry> newGradeEntriesMap = createMapForGradeEntries(newGradeEntries);
            Map<String, GradeEntry> dbGradeEntriesMap = createMapForGradeEntries(daoSession.getGradeEntryDao().loadAll());

            final List<GradeEntry> toInsert = new ArrayList<>();
            final List<GradeEntry> toUpdate = new ArrayList<>();

            // iterate new and check with equals
            for (String key : newGradeEntriesMap.keySet()) {
                // get from db map
                GradeEntry gradeEntry = dbGradeEntriesMap.get(key);

                // if not present -> new entry
                if (gradeEntry == null) {
                    GradeEntry newGradeEntry = newGradeEntriesMap.get(key);
                    newGradeEntry.setSeen(initialScraping ? Constants.GRADE_ENTRY_SEEN : Constants.GRADE_ENTRY_NEW);
                    toInsert.add(newGradeEntry);
                } else {
                    // if there -> compare and only add to update list if values changed
                    GradeEntry newGradeEntry = newGradeEntriesMap.get(key);
                    if (!gradeEntry.equals(newGradeEntry)) {
                        gradeEntry.updateGradeEntryFromOther(newGradeEntry);
                        gradeEntry.setSeen(Constants.GRADE_ENTRY_UPDATED);
                        toUpdate.add(gradeEntry);
                    }
                }
            }

            Log.d(TAG, "to insert: " + toInsert);
            Log.d(TAG, "to update: " + toUpdate);

            if(automaticScraping) {
                NotificationProcessor notificationProcessor = new NotificationProcessor(context);
                notificationProcessor.showNotificationForGrades(toInsert, toUpdate);
            }

            daoSession.runInTx(new Runnable() {
                @Override
                public void run() {
                    for (GradeEntry g : toInsert) {
                        daoSession.getGradeEntryDao().insert(g);
                    }
                    for (GradeEntry g : toUpdate) {
                        daoSession.getGradeEntryDao().update(g);
                    }
                }
            });
        }
    }

    /**
     * Creates map Hash -> GradeEntry for all GradeEntries in given list.
     * @param gradeEntries list of gradeEntries
     * @return HashMap of Hash -> GradeEntry
     */
    private Map<String, GradeEntry> createMapForGradeEntries(List<GradeEntry> gradeEntries) {
        Map<String, GradeEntry> gradeEntriesMap = new HashMap<>();
        // add all: GradeHash -> GradeEntry
        for (GradeEntry gradeEntry : gradeEntries) {
            gradeEntriesMap.put(gradeEntry.getHash(), gradeEntry);
        }
        return gradeEntriesMap;
    }

    /**
     * Load all grades from the database and post an event with all grades and semester mapping.
     */
    public void getGradesFromDatabase(boolean sticky) {
        List<GradeEntry> gradeEntries = daoSession.getGradeEntryDao().loadAll();

        // post event with new grades to subscribers
        Map<String, Integer> semesterNumberMap = getSemesterNumberMap(gradeEntries);
        String actualFirstSemester = getActualFirstSemester(gradeEntries, semesterNumberMap);
        GradesEvent gradesEvent = new GradesEvent(gradeEntries, semesterNumberMap, actualFirstSemester);
        if (sticky) {
            EventBus.getDefault().postSticky(gradesEvent);
        } else {
            EventBus.getDefault().post(gradesEvent);
        }
    }

    /**
     * Get the selected rule from the user.
     *
     * @param prefs shared preferences
     * @return selected rule
     */
    private Rule getUserRule(SharedPreferences prefs) {
        long ruleId = prefs.getLong(Constants.PREF_KEY_RULE_ID, -1);
        return daoSession.getRuleDao().queryBuilder().where(RuleDao.Properties.RuleId.eq(ruleId)).unique();
    }

    /**
     * Update user university (from shared preferences) via rest.
     * @param prefs - shared preferences
     */
    private void updateUniversity(SharedPreferences prefs) {
        long universityId = prefs.getLong(Constants.PREF_KEY_UNIVERSITY_ID, -1);

        // update university and rules
        UniversityProcessor universityProcessor = new UniversityProcessor(context);
        universityProcessor.getDetailedUniversity(universityId);
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
     * Iterates through all actions and replaces placeholder with values from gradeEntry.
     *
     * @param gradeEntry values to set for placeholders are retrieved from gradeEntry
     * @param actions actions which parseExpressions should get replaced
     */
    private void replacePlaceholdersInActions(GradeEntry gradeEntry, List<Action> actions) {
        // iterate through actions (type == table_overview) and search for placeholders
        for (Action action : actions) {
            if (action.getType() != null && action.getType().equals(ACTION_TYPE_TABLE_OVERVIEW)) {
                String parseExpression = action.getParseExpression();
                Pattern patternExamId = Pattern.compile("###" + Transformer.EXAM_ID + "###");
                Pattern patternName = Pattern.compile("###"+Transformer.NAME+"###");
                Pattern patternAttempt = Pattern.compile("###"+Transformer.ATTEMPT+"###");

                // replace placeholders
                if (gradeEntry.getExamId() != null) {
                    parseExpression = patternExamId.matcher(parseExpression).replaceAll(gradeEntry.getExamId());
                }
                if (gradeEntry.getName() != null) {
                    parseExpression = patternName.matcher(parseExpression).replaceAll(gradeEntry.getName());
                }
                if (gradeEntry.getAttempt() != null) {
                    parseExpression = patternAttempt.matcher(parseExpression).replaceAll(gradeEntry.getAttempt());
                }
                action.setParseExpression(parseExpression);
            }
        }
    }

    /**
     * Listens for IntermediateTableScrapingResult Event.
     * Transforms and saves GradeEntries to Database in separate thread.
     *
     * @param event IntermediateTableScrapingResultEvent containing a string of table with grades
     */
    @SuppressWarnings("unused")
    public void onEventAsync(IntermediateTableScrapingResultEvent event){
        Log.d("Async", "GradeHash: " + gradeHash + " - GradeHash Event: " + event.getGradeHash());
        if (this.gradeHash == null || !gradeHash.equals(event.getGradeHash())) {
            Log.d("Async", "    --> ignoring this!");
            return; // ignore event
        }

        // get shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // get rule for user
        Rule rule = getUserRule(prefs);

        try {
            List<GradeEntry> gradeEntries;

            // init Parser
            Parser parser = new Parser(context);

            // start transforming
            Transformer transformer = new Transformer(rule, event.getParsedTable(), parser);
            gradeEntries = transformer.transform();

            // save grade entries in database
            saveGradeEntriesToDB(gradeEntries, false, false);

            // save last_updated_at timestamp
            saveLastUpdatedAt(prefs);

            daoSession.clear();
            gradeEntries = daoSession.getGradeEntryDao().loadAll();

            // post event with all grades to activity
            Map<String, Integer> semesterNumberMap = getSemesterNumberMap(gradeEntries);
            String actualFirstSemester = getActualFirstSemester(gradeEntries, semesterNumberMap);
            GradesEvent gradesEvent = new GradesEvent(gradeEntries, semesterNumberMap, actualFirstSemester);
            EventBus.getDefault().postSticky(gradesEvent);
        } catch (Exception e) {
            // ignore exceptions
            Log.e(TAG, "exception while Parsing table in separate thread", e);
        }
    }

    /**
     * Post an ErrorEvent on the Event Bus only if automaticScraping is false.
     * @param type type of the Error
     * @param msg Message of the error
     * @param automaticScraping automatic scraping
     */
    private void postErrorEvent(ErrorEvent.ErrorType type, String msg, boolean automaticScraping) {
        if (!automaticScraping) {
            super.postErrorEvent(type, msg);
        } else {
            setAlarmErrorCounter();
        }
    }

    /**
     * Post an ErrorEvent on the Event Bus only if automaticScraping is false.
     * @param type type of the Error
     * @param msg Message of the error
     * @param e Exception which was raised
     * @param automaticScraping automatic scraping
     */
    private void postErrorEvent(ErrorEvent.ErrorType type, String msg, Exception e, boolean automaticScraping) {
        if (!automaticScraping) {
            super.postErrorEvent(type, msg, e);
        } else {
            setAlarmErrorCounter();
        }
    }

    /**
     * Sets one time fallback alarm in ScrapeAlarmManager.
     */
    private void setAlarmErrorCounter() {
        ScrapeAlarmManager scrapeAlarmManager = new ScrapeAlarmManager(context);
        scrapeAlarmManager.setOneTimeFallbackAlarm(true);
    }

    private Map<String,Integer> getSemesterNumberMap(List<GradeEntry> gradeEntries) {
        return semesterMapper.getSemesterToNumberMap(gradeEntries);
    }

    private String getActualFirstSemester(List<GradeEntry> gradeEntries, Map<String, Integer> semesterNumberMap) {
        return semesterMapper.getActualFirstSemester(gradeEntries, semesterNumberMap);
    }
}
