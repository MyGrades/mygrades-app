package de.mygrades.main.processor;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import de.mygrades.database.dao.University;
import de.mygrades.database.dao.UniversityDao;
import de.mygrades.main.scraping.Scraper;
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


        Scraper scraper = new Scraper(u.getRules().get(0).getActions());
        try {
            scraper.scrape();
        } catch (IOException e) {
            Log.e(TAG, "Scrape Error", e);
        } catch (ParseException e) {
            Log.e(TAG, "Parse Error", e);
        }
    }

}
