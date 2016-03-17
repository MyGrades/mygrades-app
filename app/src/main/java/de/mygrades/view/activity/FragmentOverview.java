package de.mygrades.view.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.database.dao.GradeEntry;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.ErrorEvent;
import de.mygrades.main.events.GradesEvent;
import de.mygrades.main.events.ScrapeProgressEvent;
import de.mygrades.util.Constants;
import de.mygrades.view.PtrHeader;
import de.mygrades.view.UIHelper;
import de.mygrades.view.adapter.GradesRecyclerViewAdapter;
import de.mygrades.view.adapter.dataprovider.FaqDataProvider;
import de.mygrades.view.adapter.model.GradeItem;
import de.mygrades.view.decoration.GradesDividerItemDecoration;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Fragment to show the overview of grades with a summary at the top.
 */
public class FragmentOverview extends Fragment {
    private MainServiceHelper mainServiceHelper;

    private RecyclerView rvGrades;
    private GradesRecyclerViewAdapter adapter;

    private PtrFrameLayout ptrFrame;
    private PtrHeader ptrHeader;

    // snackbar buttons
    private View.OnClickListener tryAgainListener;
    private View.OnClickListener goToFaqListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        mainServiceHelper = new MainServiceHelper(getContext());

        // init recycler view
        initGradesRecyclerView(view);

        // init pull to refresh layout
        initPullToRefresh(view);

        showInfoBox();

        // init tryAgainButton for snackbar
        tryAgainListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ptrFrame.autoRefresh();
                mainServiceHelper.scrapeForGrades(false);
            }
        };

        // init goToFaqButton for snackbar
        goToFaqListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof ReplacableFragment) {
                    FragmentFaq fragmentFaq = new FragmentFaq();
                    Bundle bundle = new Bundle();
                    bundle.putInt(FragmentFaq.ARGUMENT_GO_TO_QUESTION, FaqDataProvider.GO_TO_GENERAL_ERROR);
                    fragmentFaq.setArguments(bundle);

                    // replace fragment
                    ((ReplacableFragment) getActivity()).replaceFragment(R.id.fl_content, fragmentFaq, false);
                }
            }
        };

        // register event bus
        EventBus.getDefault().register(this);

        // get all grades
        mainServiceHelper.getGradesFromDatabase();

        // restore instance state if necessary
        ptrHeader.restoreInstanceState(savedInstanceState, ptrFrame);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ptrHeader.saveInstanceState(outState);
    }

    /**
     * Initialize the pull to refresh layout.
     */
    private void initPullToRefresh(View rootView) {
        ptrFrame = (PtrFrameLayout) rootView.findViewById(R.id.pull_to_refresh);
        ptrHeader = new PtrHeader(getContext(), getString(R.string.ptr_header_refreshing_grades));
        ptrFrame.addPtrUIHandler(ptrHeader);
        ptrFrame.setHeaderView(ptrHeader);

        ptrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                if (!ptrFrame.isAutoRefresh()) {
                    mainServiceHelper.scrapeForGrades(false);
                }
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });
    }

    /**
     * Initialize the RecyclerView and set its adapter.
     */
    private void initGradesRecyclerView(View rootView) {
        rvGrades = (RecyclerView) rootView.findViewById(R.id.rv_grades);
        rvGrades.setLayoutManager(new LinearLayoutManager(rvGrades.getContext()));
        rvGrades.addItemDecoration(new GradesDividerItemDecoration(getContext(), R.drawable.grade_divider, R.drawable.semester_divider));
        rvGrades.setItemAnimator(new DefaultItemAnimator());

        // set adapter
        adapter = new GradesRecyclerViewAdapter(getContext());
        rvGrades.setAdapter(adapter);
    }

    /**
     * Decides if an info box should be shown.
     */
    private void showInfoBox() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean dismissedNotificationInfo = prefs.getBoolean(Constants.PREF_KEY_DISMISSED_NOTIFICATION_INFO, false);
        if (!dismissedNotificationInfo) {
            String title = getString(R.string.info_notifications_title, "");
            String message = getString(R.string.info_notifications_message, "");
            adapter.showInfoBox(title, message, Constants.PREF_KEY_DISMISSED_NOTIFICATION_INFO);
        }

        int counter = prefs.getInt(Constants.PREF_KEY_APPLICATION_LAUNCHES_COUNTER, 0);
        if (counter == 0) {
            return;
        }

        // show donation info at 8 or multiples of 20 app launches
        boolean dismissedDonationInfo = prefs.getBoolean(Constants.PREF_KEY_DISMISSED_DONATION_INFO, false);
        if (counter == 8 || counter % 20 == 0) {
            if (!dismissedDonationInfo)
                showDonationInfo(prefs);
        } else {
            prefs.edit().putBoolean(Constants.PREF_KEY_DISMISSED_DONATION_INFO, false).apply();
        }

        // show rating info at 15 app launches
        boolean dismissedRatingInfo = prefs.getBoolean(Constants.PREF_KEY_DISMISSED_RATING_INFO, false);
        if (counter == 15) {
            if (!dismissedRatingInfo)
                showRatingInfo(prefs);
        } else {
            prefs.edit().putBoolean(Constants.PREF_KEY_DISMISSED_RATING_INFO, false);
        }
    }

    /**
     * Shows the donation info box.
     *
     * @param prefs shared preferences.
     */
    private void showDonationInfo(SharedPreferences prefs) {
        prefs.edit().putBoolean(Constants.PREF_KEY_DISMISSED_DONATION_INFO, false).apply();

        String title = getString(R.string.info_donation_title, "");
        String message = getString(R.string.info_donation_message, "");
        adapter.showInfoBox(title, message, Constants.PREF_KEY_DISMISSED_DONATION_INFO);
    }

    /**
     * Shows the rating info box.
     *
     * @param prefs shared preferences.
     */
    private void showRatingInfo(SharedPreferences prefs) {
        prefs.edit().putBoolean(Constants.PREF_KEY_DISMISSED_RATING_INFO, false).apply();

        String title = getString(R.string.info_rating_title, "");
        String message = getString(R.string.info_rating_message, "");
        adapter.showInfoBox(title, message, Constants.PREF_KEY_DISMISSED_RATING_INFO);
    }

    /**
     * Receive an event about the scraping progress and set the ProgressWheel accordingly.
     *
     * @param scrapeProgressEvent ScrapeProgressEvent
     */
    public void onEventMainThread(ScrapeProgressEvent scrapeProgressEvent) {
        if (ptrHeader != null && !scrapeProgressEvent.isScrapeForOverview()) {
            ptrHeader.increaseProgress(scrapeProgressEvent.getCurrentStep(), scrapeProgressEvent.getStepCount());
        }
    }

    /**
     * Receive an GradesEvent and add all grades to the adapter.
     *
     * @param gradesEvent - grades event
     */
    public void onEventMainThread(GradesEvent gradesEvent) {
        if (adapter != null) {
            for(GradeEntry gradeEntry : gradesEvent.getGrades()) {
                GradeItem item = new GradeItem(gradeEntry);
                String semester = gradeEntry.getSemester();
                String modifiedSemester = gradeEntry.getModifiedSemester();
                semester = modifiedSemester == null ? semester : modifiedSemester;

                Integer semesterNumber = gradeEntry.getSemesterNumber();
                Integer modifiedSemesterNumber = gradeEntry.getModifiedSemesterNumber();
                semesterNumber = modifiedSemesterNumber == null ? semesterNumber : modifiedSemesterNumber;
                adapter.addGradeForSemester(item, semesterNumber, semester);
            }

            // update the summary header
            adapter.updateSummary();
        }

        // if it's an result of scraping
        if (gradesEvent.isScrapingResult() && ptrFrame != null && ptrHeader != null) {
            UIHelper.showSnackbar(getView(), getString(R.string.snackbar_refresh_complete));
            ptrFrame.refreshComplete();
        }

        // remove from sticky events
        EventBus.getDefault().removeStickyEvent(gradesEvent);
    }

    /**
     * Receive an ErrorEvent and display it to the user.
     *
     * @param errorEvent - ErrorEvent
     */
    public void onEventMainThread(ErrorEvent errorEvent) {
        if (ptrFrame != null && ptrHeader != null) {
            ptrHeader.setIsError(true);
            ptrFrame.refreshComplete();
        }

        UIHelper.displayErrorMessage(getView(), errorEvent, tryAgainListener, goToFaqListener);
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
