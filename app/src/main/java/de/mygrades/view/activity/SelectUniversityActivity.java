package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.List;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.database.dao.University;
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

        // get all published universities from the database
        mainServiceHelper.getUniversitiesFromDatabase(true);

        // get all published universities from server
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
     * Add new universities to the adapter.
     *
     * @param universities list of new universities which should be added
     */
    private void addUniversities(List<University> universities) {
        for(University university : universities) {
            UniversityItem universityItem = new UniversityItem(university.getName(), university.getUniversityId());
            universityAdapter.add(universityItem);
        }

        if (universityAdapter.getItemCount() > 0 && swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }
    }

    /**
     * Receive an UniversityEvent and add all new universities to the adapter.
     *
     * @param universityEvent - university event
     */
    public void onEventMainThread(UniversityEvent universityEvent) {
        if (universityAdapter != null) {
            addUniversities(universityEvent.getNewUniversities(true));
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
