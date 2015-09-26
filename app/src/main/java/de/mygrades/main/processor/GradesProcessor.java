package de.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.mygrades.database.dao.GradeEntry;
import de.mygrades.database.dao.Rule;
import de.mygrades.database.dao.University;
import de.mygrades.database.dao.UniversityDao;
import de.mygrades.main.core.Parser;
import de.mygrades.main.core.Scraper;
import de.mygrades.main.core.Transformer;
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

        // init parser only 1 time
        Parser parser = null;
        try {
           parser = new Parser(context);
        } catch (ParseException e) {
            Log.e(TAG, "Parser Error", e);
        }

        String scrapingResult = null;
        Scraper scraper = new Scraper(rule.getActions(), parser);
        try {
            scrapingResult = scraper.scrape();
        } catch (IOException e) {
            Log.e(TAG, "Scrape Error", e);
        } catch (ParseException e) {
            Log.e(TAG, "Parse Error", e);
        }

        Transformer transformer = new Transformer(rule.getTransformerMappings(), scrapingResult, parser);
        List<GradeEntry> gradeEntries = null;
        try {
            gradeEntries = transformer.transform();
        } catch (ParseException e) {
            Log.e(TAG, "Transform Error", e);
        }

        // send event with new grades to activity
        GradesEvent gradesEvent = new GradesEvent();
        gradesEvent.setGrades(gradeEntries);
        EventBus.getDefault().post(gradesEvent);
    }
}
