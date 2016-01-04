package de.mygrades.main.processor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.event.EventBus;
import de.mygrades.database.dao.Action;
import de.mygrades.database.dao.ActionDao;
import de.mygrades.database.dao.ActionParam;
import de.mygrades.database.dao.ActionParamDao;
import de.mygrades.database.dao.Rule;
import de.mygrades.database.dao.TransformerMapping;
import de.mygrades.database.dao.TransformerMappingDao;
import de.mygrades.database.dao.University;
import de.mygrades.database.dao.UniversityDao;
import de.mygrades.main.events.ErrorEvent;
import de.mygrades.main.events.UniversityEvent;
import de.mygrades.util.Constants;
import retrofit.RetrofitError;

/**
 * UniversityProcessor is responsible for university resources.
 * It makes rest calls and required inserts / updates to the local database.
 */
public class UniversityProcessor extends BaseProcessor {
    private static final String TAG = UniversityProcessor.class.getSimpleName();

    public UniversityProcessor(Context context) {
        super(context);
    }

    /**
     * Load all universities from the server and posts an UniversityEvent.
     *
     * @param publishedOnly - load only published universities or all
     */
    public void getUniversities(boolean publishedOnly) {
        // No Connection -> event no Connection, abort
        if (!isOnline()) {
            postErrorEvent(ErrorEvent.ErrorType.NO_NETWORK, "No Internet Connection!");
            return;
        }

        try {
            String updatedAtServerPublished = getUpdatedAtServerForUniversities(true);
            String updatedAtServerUnpublished = getUpdatedAtServerForUniversities(false);

            // make synchronous rest call
            List<University> universities = restClient.getRestApi().getUniversities(publishedOnly, updatedAtServerPublished, updatedAtServerUnpublished);
            universities = universities == null ? new ArrayList<University>() : universities;

            // insert universities into database
            final List<University> finalUniversities = universities;
            daoSession.runInTx(new Runnable() {
                @Override
                public void run() {
                    for (University university : finalUniversities) {
                        daoSession.getUniversityDao().insertOrReplace(university);

                        // insert rules into database, important: only rule_id and name are inserted
                        for (Rule rule : university.getRulesRaw()) {
                            Rule ruleInDb = daoSession.getRuleDao().load(rule.getRuleId());
                            if (ruleInDb == null) {
                                // insert rule only if it not exists already
                                daoSession.getRuleDao().insert(rule);
                            } else {
                                // update rule name only
                                ruleInDb.setName(rule.getName());
                                daoSession.getRuleDao().update(ruleInDb);
                            }
                        }
                    }
                }
            });

            // post university event
            UniversityEvent universityEvent = new UniversityEvent();
            universityEvent.setUniversities(universities);
            EventBus.getDefault().post(universityEvent);
        } catch (RetrofitError e) {
            if (e.getCause() instanceof ConnectException) {
                postErrorEvent(ErrorEvent.ErrorType.TIMEOUT, "Timeout", e);
            } else {
                postErrorEvent(ErrorEvent.ErrorType.GENERAL, "General Error", e);
            }
            Log.e(TAG, "RetrofitError: " + e.getMessage());
        }
    }

    /**
     * Load all universities from the database and posts an UniversityEvent.
     *
     * @param publishedOnly - select only published universities or all
     */
    public void getUniversitiesFromDatabase(boolean publishedOnly) {
        UniversityDao universityDao = daoSession.getUniversityDao();

        List<University> universities = universityDao.queryBuilder()
                .where(UniversityDao.Properties.Published.eq(publishedOnly))
                .orderAsc(UniversityDao.Properties.Name)
                .list();

        // eager load all rules
        for (University university : universities) {
            university.getRules();
        }

        // post university event
        UniversityEvent universityEvent = new UniversityEvent();
        universityEvent.setUniversities(universities);
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
                        clearRule(rule); // delete actions and transformer mappings

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
     * Deletes all ActionParams, Actions and TransformerMappings for a given rule from database.
     *
     * @param rule
     */
    private void clearRule(Rule rule) {
        DeleteQuery deleteActionParams = daoSession.getActionParamDao().queryBuilder()
                .where(ActionParamDao.Properties.ActionId.eq(-1))
                .buildDelete();

        DeleteQuery deleteActions = daoSession.getActionDao().queryBuilder()
                .where(ActionDao.Properties.RuleId.eq(rule.getRuleId()))
                .buildDelete();

        DeleteQuery deleteTransformerMappings = daoSession.getTransformerMappingDao().queryBuilder()
                .where(TransformerMappingDao.Properties.RuleId.eq(rule.getRuleId()))
                .buildDelete();

        // delete actionParams
        for (Action action : rule.getActionsRaw()) {
            deleteActionParams.setParameter(0, action.getActionId());
            deleteActionParams.executeDeleteWithoutDetachingEntities();
        }

        // delete actions
        deleteActions.executeDeleteWithoutDetachingEntities();

        // delete transformer mappings
        deleteTransformerMappings.executeDeleteWithoutDetachingEntities();

        daoSession.clear();
    }

    /**
     * Get the latest updated_at_server timestamp for all universities.
     * The selected university should be excluded from the query, because
     * it may be already updated multiple times.
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
     * Return null, if the selected university has no rules with actions attached (should only happen at first load).
     *
     * @param universityId - university id
     * @return timestamp as string
     */
    private String getUpdatedAtServerForUniversity(long universityId) {
        University u = daoSession.getUniversityDao().queryBuilder().where(UniversityDao.Properties.UniversityId.eq(universityId)).unique();

        // should not happen
        if (u == null || u.getRules() == null || u.getRules().size() == 0) {
            return null;
        }

        // check if each rule has actions, otherwise return null
        if (u.getRules().size() > 0) {
            for (Rule rule : u.getRules()) {
                if (rule.getActions() == null || rule.getActions().size() == 0) {
                    return null;
                }
            }
        }

        return u.getUpdatedAtServer();
    }
}
