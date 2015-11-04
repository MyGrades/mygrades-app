package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.database.dao.University;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.ErrorEvent;
import de.mygrades.main.events.UniversityEvent;
import de.mygrades.view.adapter.UniversitiesRecyclerViewAdapter;
import de.mygrades.view.adapter.model.UniversityItem;
import de.mygrades.view.decoration.UniversityDividerItemDecoration;

/**
 * Activity which shows all universities.
 * The user can select a university to be forwarded to the LoginActivity.
 */
public class SelectUniversityActivity extends AppCompatActivity {

    private RecyclerView rvUniversities;
    private UniversitiesRecyclerViewAdapter universityAdapter;
    private FrameLayout flLoading;
    private FrameLayout flErrorWrapper;
    private TextView tvErrorMessage;
    private Button btnTryAgain;

    private static final String ERROR_TYPE_STATE = "error_type_state";
    private ErrorEvent.ErrorType actErrorType;

    private MainServiceHelper mainServiceHelper;

    public SelectUniversityActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_university);
        mainServiceHelper = new MainServiceHelper(this);

        flLoading = (FrameLayout) findViewById(R.id.fl_loading);
        flErrorWrapper = (FrameLayout) findViewById(R.id.fl_error_wrapper);
        tvErrorMessage = (TextView) findViewById(R.id.tv_error_message);
        btnTryAgain = (Button) findViewById(R.id.btn_try_again);
        initTryAgainButton();

        // init toolbar
        initToolbar();

        // init recycler view
        initRecyclerView();

        // register event bus
        EventBus.getDefault().register(this);

        // restore instance state
        if (savedInstanceState != null) {
            String error = savedInstanceState.getString(ERROR_TYPE_STATE);
            actErrorType = error == null ? null : ErrorEvent.ErrorType.valueOf(error);

            if (actErrorType != null) {
                showErrorWrapper(actErrorType);
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
        if (actErrorType != null) {
            outState.putString(ERROR_TYPE_STATE, actErrorType.name());
        }
    }

    /**
     * Initialize the try-again button.
     */
    private void initTryAgainButton() {
        btnTryAgain.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                flErrorWrapper.setVisibility(View.GONE);
                flLoading.setVisibility(View.VISIBLE);
                actErrorType = null;

                // load universities from server
                mainServiceHelper.getUniversities(true);
            }
        });
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
    private void initRecyclerView() {
        rvUniversities = (RecyclerView) findViewById(R.id.rv_universities);
        rvUniversities.setLayoutManager(new LinearLayoutManager(rvUniversities.getContext()));
        rvUniversities.addItemDecoration(new UniversityDividerItemDecoration(this, R.drawable.university_divider));
        rvUniversities.setItemAnimator(new DefaultItemAnimator());
        universityAdapter = new UniversitiesRecyclerViewAdapter();
        rvUniversities.setAdapter(universityAdapter);
    }

    /**
     * Add new universities to the adapter.
     * If the adapter is empty, an indeterminate progress animation will be shown continuously.
     *
     * @param universities list of new universities which should be added
     */
    private void addUniversities(List<University> universities) {
        if (universities.size() > 0) {
            flErrorWrapper.setVisibility(View.GONE);
        }

        for(University university : universities) {
            UniversityItem universityItem = new UniversityItem(university.getName(), university.getUniversityId());
            universityAdapter.add(universityItem);
        }

        // show loading animation only if adapter is empty and no error is currently shown
        if (actErrorType == null) {
            flLoading.setVisibility(universityAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
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

    /**
     * Receive error events and show an info text and retry-button.
     *
     * @param errorEvent ErrorEvent
     */
    public void onEventMainThread(ErrorEvent errorEvent) {
        if (universityAdapter.getItemCount() > 0) {
            // ignore error
            return;
        }

        actErrorType = errorEvent.getType();
        showErrorWrapper(errorEvent.getType());
    }

    /**
     * Shows the error wrapper with a TextView and Button.
     *
     * @param errorType ErrorEvent.ErrorType
     */
    private void showErrorWrapper(ErrorEvent.ErrorType errorType) {
        // hide loading wrapper
        flLoading.setVisibility(View.GONE);

        // show error wrapper
        flErrorWrapper.setVisibility(View.VISIBLE);

        String errorMessage;
        switch (errorType) {
            case NO_NETWORK:
                errorMessage = getResources().getString(R.string.error_no_network);
                break;
            case TIMEOUT:
                errorMessage = getResources().getString(R.string.error_server_timeout);
                break;
            case GENERAL:
            default:
                errorMessage = getResources().getString(R.string.error_unknown);
        }

        tvErrorMessage.setText(errorMessage);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
