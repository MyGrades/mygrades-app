package de.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.database.dao.Action;
import de.mygrades.database.dao.ActionDao;
import de.mygrades.database.dao.ActionParam;
import de.mygrades.database.dao.Rule;
import de.mygrades.database.dao.TransformerMapping;
import de.mygrades.database.dao.University;
import de.mygrades.database.dao.UniversityDao;
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
    public void getUniversities() {
        List<University> universities = new ArrayList<>();

        // make synchronous rest call
        try {
            String updatedAtServer = getUpdatedAtServerForUniversities();
            universities = restClient.getRestApi().getUniversities(updatedAtServer);
        } catch (RetrofitError e) {
            Log.e(TAG, "RetrofitError: " + e.getMessage());
        }

        // insert into database
        daoSession.getUniversityDao().insertOrReplaceInTx(universities);
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

        //Log.v("processor", "get detailed university, rules: " + university.getRulesRaw().size());

        // insert into database
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

    /**
     * Get the latest updated_at_server timestamp for all universities.
     *
     * @return timestamp as string
     */
    private String getUpdatedAtServerForUniversities() {
        // get selected university id to exclude it from query
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long universityId = prefs.getLong(Constants.PREF_KEY_UNIVERSITY_ID, -1);

        University u = daoSession.getUniversityDao().queryBuilder()
                .orderDesc(UniversityDao.Properties.UpdatedAtServer)
                .where(UniversityDao.Properties.UniversityId.notEq(universityId))
                .limit(1)
                .unique();

        if (u == null) {
            return u.getUpdatedAtServer();
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
