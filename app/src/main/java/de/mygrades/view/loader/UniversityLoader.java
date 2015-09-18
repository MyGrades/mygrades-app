package de.mygrades.view.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

import de.mygrades.MyGradesApplication;
import de.mygrades.database.dao.DaoSession;
import de.mygrades.database.dao.University;
import de.mygrades.database.dao.UniversityDao;

/**
 * Loader to load all universities.
 */
public class UniversityLoader extends AsyncTaskLoader<List<University>> {

    private DaoSession daoSession;

    public UniversityLoader(Context context) {
        super(context);
        daoSession = ((MyGradesApplication) context.getApplicationContext()).getDaoSession();
    }

    @Override
    protected void onStartLoading() {
        forceLoad(); // TODO: hacky, only forceLoad if content is new
    }

    @Override
    public List<University> loadInBackground() {
        UniversityDao universityDao = daoSession.getUniversityDao();
        return universityDao.queryBuilder().orderAsc(UniversityDao.Properties.Name).list();
    }
}
