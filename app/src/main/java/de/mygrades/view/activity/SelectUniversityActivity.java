package de.mygrades.view.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.List;

import de.greenrobot.event.EventBus;
import de.mygrades.MyGradesApplication;
import de.mygrades.R;
import de.mygrades.database.dao.DaoSession;
import de.mygrades.database.dao.University;
import de.mygrades.database.dao.UniversityDao;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.UniversityEvent;
import de.mygrades.view.adapter.UniversitiesRecyclerViewAdapter;
import de.mygrades.view.adapter.model.UniversityItem;
import de.mygrades.view.decoration.DividerItemDecoration;

/**
 * Activity which shows all universities.
 */
public class SelectUniversityActivity extends AppCompatActivity {

    private RecyclerView rvUniversities;
    private UniversitiesRecyclerViewAdapter universityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_university);

        // set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(getResources().getString(R.string.app_name));

        // set recycler view
        rvUniversities = (RecyclerView) findViewById(R.id.rv_universities);
        rvUniversities.setLayoutManager(new LinearLayoutManager(rvUniversities.getContext()));
        rvUniversities.addItemDecoration(new DividerItemDecoration(this, R.drawable.university_divider));
        rvUniversities.setItemAnimator(new DefaultItemAnimator());
        universityAdapter = new UniversitiesRecyclerViewAdapter();
        rvUniversities.setAdapter(universityAdapter);

        // start async task to load universities
        UniversityAsyncTask universityAsyncTask = new UniversityAsyncTask(this);
        universityAsyncTask.execute();

        // register event bus
        EventBus.getDefault().register(this);

        // get all universities from server
        MainServiceHelper mainServiceHelper = new MainServiceHelper(this);
        mainServiceHelper.getUniversities(true);
    }

    /**
     * AsyncTask to load all universities in background thread from database.
     */
    private class UniversityAsyncTask extends AsyncTask<Void, Void, List<University>> {
        private Context context;

        public UniversityAsyncTask(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        protected List<University> doInBackground(Void... params) {
            DaoSession daoSession = ((MyGradesApplication) context.getApplicationContext()).getDaoSession();
            UniversityDao universityDao = daoSession.getUniversityDao();

            List<University> universities = universityDao.queryBuilder()
                    .where(UniversityDao.Properties.Published.eq(true))
                    .orderAsc(UniversityDao.Properties.Name)
                    .list();

            // set section flags for universities
            for(int i = 0; i < universities.size(); i++) {
                String prefix = universities.get(i).getName().substring(0, 1).toUpperCase();
                String prevPrefix = null;
                if (i > 0) {
                    prevPrefix = universities.get(i-1).getName().substring(0, 1).toUpperCase();
                }

                if (!prefix.equals(prevPrefix)) {
                    universities.get(i).setSection(true);
                }
            }

            return universities;
        }

        @Override
        protected void onPostExecute(List<University> universities) {
            addUniversities(universities);
        }
    }

    private void addUniversities(List<University> universities) {
        for(University university : universities) {
            UniversityItem universityItem = new UniversityItem(university.getName(), university.getUniversityId());
            universityAdapter.add(universityItem);
        }
    }

    /**
     * Receive an UniversityEvent and add all new universities to the adapter.
     *
     * @param universityEvent - university event
     */
    public void onEvent(final UniversityEvent universityEvent) {
        if (universityAdapter != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addUniversities(universityEvent.getNewUniversities(true));
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
