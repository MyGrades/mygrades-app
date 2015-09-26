package de.mygrades.main.processor;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import de.mygrades.database.dao.GradeEntry;
import de.mygrades.database.dao.TransformerMapping;
import de.mygrades.database.dao.University;
import de.mygrades.database.dao.UniversityDao;
import de.mygrades.main.core.Scraper;
import de.mygrades.main.core.Parser;
import de.mygrades.main.core.Transformer;
import de.mygrades.main.events.GradesEvent;
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
        University u = daoSession.getUniversityDao().queryBuilder().where(UniversityDao.Properties.UniversityId.eq(333l)).unique();
        Log.v(TAG, "rules: " + u.getRules().size());
        Log.v(TAG, "actions: "+ u.getRules().get(0).getActions().size());
        Log.v(TAG, "params: "+ u.getRules().get(0).getActions().get(1).getActionParams().size());
        Log.v(TAG, "transformer_mapping: "+ u.getRules().get(0).getTransformerMappings().toString());

        // init parser only 1 time
        Parser parser = null;
        try {
           parser = new Parser();
        } catch (ParseException e) {
            Log.e(TAG, "Parser Error", e);
        }

        String scrapingResult = null;
        Scraper scraper = new Scraper(u.getRules().get(0).getActions(), parser);
        try {
            scrapingResult = scraper.scrape();
        } catch (IOException e) {
            Log.e(TAG, "Scrape Error", e);
        } catch (ParseException e) {
            Log.e(TAG, "Parse Error", e);
        }

        Transformer transformer = new Transformer(u.getRules().get(0).getTransformerMappings(), scrapingResult, parser);
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
