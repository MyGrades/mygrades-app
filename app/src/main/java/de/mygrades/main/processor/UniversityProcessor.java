package de.mygrades.main.processor;

import android.content.Context;
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
            universities = restClient.getRestApi().getUniversities();
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
            university = restClient.getRestApi().getUniversity(universityId);
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
                }
            }
        });

        /* TODO: für jonas den lurch
        University u = daoSession.getUniversityDao().queryBuilder().where(UniversityDao.Properties.UniversityId.eq(333l)).unique();
        Log.v(TAG, "rules: "+ u.getRules().size());
        Log.v(TAG, "actions; "+ u.getRules().get(0).getActions().size());
        Log.v(TAG, "params: "+ u.getRules().get(0).getActions().get(1).getActionParams().size());
        */
    }
}
