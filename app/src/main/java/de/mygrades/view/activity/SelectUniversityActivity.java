package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

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
public class SelectUniversityActivity extends AppCompatActivity {

    private RecyclerView rvUniversities;
    private UniversitiesRecyclerViewAdapter universityAdapter;
    private FrameLayout flLoading;

    private MainServiceHelper mainServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_university);
        mainServiceHelper = new MainServiceHelper(this);

        flLoading = (FrameLayout) findViewById(R.id.fl_loading);

        // init toolbar
        initToolbar();

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

        // show only if adapter is empty
        flLoading.setVisibility(universityAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    /**
     * Receive an UniversityEvent and add all new universities to the adapter.
     *
     * @param universityEvent - university event
     */
    public void onEventMainThread(UniversityEvent universityEvent) {
        if (universityAdapter != null) {
            addUniversities(universityEvent.getUniversities(true));
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
