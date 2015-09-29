package de.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.event.EventBus;
import de.mygrades.database.dao.Action;
import de.mygrades.database.dao.ActionDao;
import de.mygrades.database.dao.ActionParam;
import de.mygrades.database.dao.Rule;
import de.mygrades.database.dao.TransformerMapping;
import de.mygrades.database.dao.University;
import de.mygrades.database.dao.UniversityDao;
import de.mygrades.main.events.UniversityEvent;
import de.mygrades.util.Constants;
import retrofit.RetrofitError;

/**
 * UniversityProcessor is responsible for university resources.
 * It makes rest calls and required inserts / updated to the local database.
 */
public class UniversityProcessor extends BaseProcessor {
    private static final String TAG = UniversityProcessor.class.getSimpleName();

    public UniversityProcessor(Context context) {
        super(context);
    }

    /**
     * Load all universities from the server.
     */
    public void getUniversities(boolean publishedOnly) {
        List<University> universities = new ArrayList<>();

        // make synchronous rest call
        try {
            String updatedAtServerPublished = getUpdatedAtServerForUniversities(true);
            String updatedAtServerUnpublished = getUpdatedAtServerForUniversities(false);
            universities = restClient.getRestApi().getUniversities(publishedOnly, updatedAtServerPublished, updatedAtServerUnpublished);
        } catch (RetrofitError e) {
            Log.e(TAG, "RetrofitError: " + e.getMessage());
        }

        universities = universities == null ? new ArrayList<University>() : universities;

        // insert into database
        daoSession.getUniversityDao().insertOrReplaceInTx(universities);

        // post university event
        UniversityEvent universityEvent = new UniversityEvent();
        universityEvent.setNewUniversities(universities);
        EventBus.getDefault().post(universityEvent);
    }

    /**
     * Load all universities from the database and post an event.
     *
     * @param publishedOnly - select only published universities or all
     */
    public void getUniversitiesFromDatabase(boolean publishedOnly) {
        UniversityDao universityDao = daoSession.getUniversityDao();

        List<University> universities = universityDao.queryBuilder()
                .where(UniversityDao.Properties.Published.eq(publishedOnly))
                .orderAsc(UniversityDao.Properties.Name)
                .list();

        // post university event
        UniversityEvent universityEvent = new UniversityEvent();
        universityEvent.setNewUniversities(universities);
        EventBus.getDefault().post(universityEvent);
    }

    /**
     * Get a detailed university with all rules, actions etc by an university id.
     *
     * @param universityId - university id
     */
    public void getDetailedUniversity(long universityId) {
        University university = null;

        try {
            String updatedAtServer = getUpdatedAtServerForUniversity(universityId);
            university = restClient.getRestApi().getUniversity(universityId, updatedAtServer);
        } catch (RetrofitError e) {
            Log.e(TAG, "RetrofitError: " + e.getMessage());
        }

        // insert into database
        if (university != null) {
            final University finalUniversity = university;
            daoSession.runInTx(new Runnable() {
                @Override
                public void run() {
                    daoSession.getUniversityDao().insertOrReplace(finalUniversity);

                    for (Rule rule : finalUniversity.getRulesRaw()) {
                        daoSession.getRuleDao().insertOrReplace(rule);

                        for (Action action : rule.getActionsRaw()) {
                            daoSession.getActionDao().insertOrReplace(action);

                            for (ActionParam actionParam : action.getActionParamsRaw()) {
                                daoSession.getActionParamDao().insertOrReplace(actionParam);
                            }
                        }

                        for (TransformerMapping transformerMapping : rule.getTransformerMappingsRaw()) {
                            daoSession.getTransformerMappingDao().insertOrReplace(transformerMapping);
                        }
                    }
                }
            });
        }
    }

    /**
     * Get the latest updated_at_server timestamp for all universities.
     *
     * @param publishedOnly - get timestamp for published or unpublished universities
     * @return timestamp as string
     */
    public String getUpdatedAtServerForUniversities(boolean publishedOnly) {
        // get selected university id to exclude it from query
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long universityId = prefs.getLong(Constants.PREF_KEY_UNIVERSITY_ID, -1);

        University university = daoSession.getUniversityDao().queryBuilder()
                .orderDesc(UniversityDao.Properties.UpdatedAtServer)
                .where(UniversityDao.Properties.UniversityId.notEq(universityId))
                .where(UniversityDao.Properties.Published.eq(publishedOnly))
                .limit(1)
                .unique();

        if (university != null) {
            return university.getUpdatedAtServer();
        }

        return null;
    }

    /**
     * Get the updated_at_server timestamp for the selected university.
     * Return null, if the selected university has no rules attached (should not happen).
     *
     * @param universityId - university id
     * @return timestamp
     */
    private String getUpdatedAtServerForUniversity(long universityId) {
        University u = daoSession.getUniversityDao().queryBuilder().where(UniversityDao.Properties.UniversityId.eq(universityId)).unique();

        if (u == null || u.getRules() == null || u.getRules().size() == 0) {
            return null;
        }

        return u.getUpdatedAtServer();
    }
}
