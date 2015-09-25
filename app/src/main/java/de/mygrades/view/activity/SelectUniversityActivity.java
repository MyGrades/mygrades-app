package de.mygrades.view.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
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
public class SelectUniversityActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private AppBarLayout appBarLayout;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvUniversities;
    private UniversitiesRecyclerViewAdapter universityAdapter;

    private MainServiceHelper mainServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_university);
        mainServiceHelper = new MainServiceHelper(this);

        // init toolbar
        initToolbar();

        // init app bar layout and swipe to refresh
        initSwipeToRefresh();

        // init recycler view
        initRecyclerView();

        // register event bus
        EventBus.getDefault().register(this);

        // start async task to load universities
        UniversityAsyncTask universityAsyncTask = new UniversityAsyncTask(this);
        universityAsyncTask.execute();

        // get all universities from server
        mainServiceHelper.getUniversities(true);
    }

    /**
     * Initialize the toolbar.
     */
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(getResources().getString(R.string.app_name));
    }

    /**
     * Initialize the swipeToRefresh layout.
     */
    private void initSwipeToRefresh() {
        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mainServiceHelper.getUniversities(true);
            }
        });
    }

    /**
     * Initialize the recycler view and set the adapter.
     */
    private void initRecyclerView() {
        rvUniversities = (RecyclerView) findViewById(R.id.rv_universities);
        rvUniversities.setLayoutManager(new LinearLayoutManager(rvUniversities.getContext()));
        rvUniversities.addItemDecoration(new DividerItemDecoration(this, R.drawable.university_divider));
        rvUniversities.setItemAnimator(new DefaultItemAnimator());
        universityAdapter = new UniversitiesRecyclerViewAdapter();
        rvUniversities.setAdapter(universityAdapter);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        // the refresh must be only active when the offset is zero
        swipeRefresh.setEnabled(i == 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appBarLayout.removeOnOffsetChangedListener(this);
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

            return universities;
        }

        @Override
        protected void onPostExecute(List<University> universities) {
            if (universities.size() > 0) {
                addUniversities(universities);
                swipeRefresh.setRefreshing(false);
            } else {
                // no universities in the database, show the refresh indicator
                swipeRefresh.setRefreshing(true);
            }
        }
    }

    /**
     * Add new universities to the adapter.
     *
     * @param universities list of new universities which should be added
     */
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
                    swipeRefresh.setRefreshing(false);
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
