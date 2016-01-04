package de.mygrades.view.activity;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.database.dao.Rule;
import de.mygrades.database.dao.University;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.ErrorEvent;
import de.mygrades.main.events.UniversityEvent;
import de.mygrades.view.adapter.UniversitiesAdapter;
import de.mygrades.view.adapter.dataprovider.UniversitiesDataProvider;
import de.mygrades.view.adapter.model.RuleItem;
import de.mygrades.view.adapter.model.UniversityItem;

/**
 * Activity which shows all universities.
 * The user can select a university to be forwarded to the LoginActivity.
 */
public class SelectUniversityActivity extends AppCompatActivity {
    private static final String SAVED_STATE_EXPANDABLE_ITEM_MANAGER = "RecyclerViewExpandableItemManager";

    private RecyclerView rvUniversities;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter wrappedAdapter;
    private RecyclerViewExpandableItemManager recyclerViewExpandableItemManager;
    private UniversitiesAdapter universitiesAdapter;
    private UniversitiesDataProvider dataProvider;

    private static final String ERROR_TYPE_STATE = "error_type_state";
    private MainServiceHelper mainServiceHelper;

    public SelectUniversityActivity() { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_university);
        mainServiceHelper = new MainServiceHelper(this);

        // init toolbar
        initToolbar();

        // init recycler view
        initRecyclerView(savedInstanceState);

        // register event bus
        EventBus.getDefault().register(this);

        // restore instance state
        if (savedInstanceState != null) {
            String error = savedInstanceState.getString(ERROR_TYPE_STATE);
            if (error != null) {
                universitiesAdapter.showError(ErrorEvent.ErrorType.valueOf(error));
            }
        }

        // get all published universities from the database
        mainServiceHelper.getUniversitiesFromDatabase(true);

        // get all published universities from server
        mainServiceHelper.getUniversities(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save error state
        ErrorEvent.ErrorType actErrorType = universitiesAdapter.getActErrorType();
        if (actErrorType != null) {
            outState.putString(ERROR_TYPE_STATE, actErrorType.name());
        }
    }

    /**
     * Initialize the toolbar.
     */
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
    }

    /**
     * Initialize the recycler view and set the adapter.
     */
    private void initRecyclerView(Bundle savedInstanceState) {
                // restore state if necessary
        final Parcelable eimSavedState = (savedInstanceState != null) ? savedInstanceState.getParcelable(SAVED_STATE_EXPANDABLE_ITEM_MANAGER) : null;
        recyclerViewExpandableItemManager = new RecyclerViewExpandableItemManager(eimSavedState);

        // create data provider
        dataProvider = new UniversitiesDataProvider();

        // create adapter
        universitiesAdapter = new UniversitiesAdapter(this, dataProvider, recyclerViewExpandableItemManager);
        wrappedAdapter = recyclerViewExpandableItemManager.createWrappedAdapter(universitiesAdapter);

        // set animation stuff
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        animator.setSupportsChangeAnimations(false);

        // init recycler view
        layoutManager = new LinearLayoutManager(this);
        rvUniversities = (RecyclerView) findViewById(R.id.rv_universities);
        rvUniversities.setLayoutManager(layoutManager);
        rvUniversities.setAdapter(wrappedAdapter);
        rvUniversities.setItemAnimator(animator);
        rvUniversities.setHasFixedSize(false);

        // attach recycler view to item manager, necessary for touch listeners
        recyclerViewExpandableItemManager.attachRecyclerView(rvUniversities);
    }

    /**
     * Add new universities to the adapter.
     * If the adapter is empty, an indeterminate progress animation will be shown continuously.
     *
     * @param universities list of new universities which should be added
     */
    private void addUniversities(List<University> universities) {
        if (universities.size() > 0) {
            universitiesAdapter.showError(null);
        }


        List<UniversityItem> universityDataList = new ArrayList<>();
        for(University university : universities) {
            UniversityItem universityData = new UniversityItem(university.getUniversityId());
            universityData.setName(university.getName());
            universityData.setUniversityId(university.getUniversityId());

            for (Rule rule : university.getRules()) {
                RuleItem ruleData = new RuleItem(rule.getRuleId());
                ruleData.setName(rule.getName());
                ruleData.setRuleId(rule.getRuleId());
                universityData.addRuleData(ruleData);
            }
            universityDataList.add(universityData);
        }
        universitiesAdapter.addUniversities(universityDataList);

        // show loading animation only if adapter is empty and no error is currently shown
        if (universitiesAdapter.getActErrorType() == null) {
            universitiesAdapter.showLoadingAnimation(universitiesAdapter.isEmpty());
        }
    }

    /**
     * Receive an UniversityEvent and add all new universities to the adapter.
     *
     * @param universityEvent - university event
     */
    public void onEventMainThread(UniversityEvent universityEvent) {
        if (wrappedAdapter != null) {
            addUniversities(universityEvent.getUniversities(true));
        }
    }

    /**
     * Receive error events and show an info text and retry-button.
     *
     * @param errorEvent ErrorEvent
     */
    public void onEventMainThread(ErrorEvent errorEvent) {
        if (!universitiesAdapter.isEmpty()) {
            // ignore error
            return;
        }

        universitiesAdapter.showError(errorEvent.getType());
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
