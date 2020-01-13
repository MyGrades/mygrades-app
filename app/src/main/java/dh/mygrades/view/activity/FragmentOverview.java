package dh.mygrades.view.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;

import de.greenrobot.event.EventBus;
import dh.mygrades.R;
import dh.mygrades.database.dao.GradeEntry;
import dh.mygrades.main.MainServiceHelper;
import dh.mygrades.main.events.DeleteGradeEvent;
import dh.mygrades.main.events.ErrorEvent;
import dh.mygrades.main.events.GradesEvent;
import dh.mygrades.main.events.ScrapeProgressEvent;
import dh.mygrades.util.Constants;
import dh.mygrades.view.PtrHeader;
import dh.mygrades.view.UIHelper;
import dh.mygrades.view.adapter.GradesRecyclerViewAdapter;
import dh.mygrades.view.adapter.dataprovider.FaqDataProvider;
import dh.mygrades.view.adapter.model.GradeItem;
import dh.mygrades.view.decoration.GradesDividerItemDecoration;
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
    private FloatingActionButton fabAddGradeEntry;

    // snackbar buttons
    private View.OnClickListener tryAgainListener;
    private View.OnClickListener goToFaqListener;

    private boolean receivedGradesEvent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        mainServiceHelper = new MainServiceHelper(getContext());
        setHasOptionsMenu(true);

        // init recycler view
        initGradesRecyclerView(view);

        // init floating action button
        initFloatingActionButton(view);

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
     * Initialize the floating action button to add new grade entries.
     */
    private void initFloatingActionButton(View rootView) {
        fabAddGradeEntry = (FloatingActionButton) rootView.findViewById(R.id.fab_add_grade_entry);
        fabAddGradeEntry.attachToRecyclerView(rvGrades);
        fabAddGradeEntry.hide();
        fabAddGradeEntry.setVisibility(View.GONE);
        fabAddGradeEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.enableEditMode(false);
                fabAddGradeEntry.hide(true);
                fabAddGradeEntry.setVisibility(View.GONE);
                getActivity().invalidateOptionsMenu();

                final Intent intent = new Intent(v.getContext(), GradeDetailedActivity.class);
                intent.putExtra(GradeDetailedActivity.EXTRA_GRADE_HASH, "");
                intent.putExtra(GradeDetailedActivity.EXTRA_ADD_NEW_GRADE_ENTRY, true);
                v.getContext().startActivity(intent);
            }
        });
    }

    /**
     * Decides if an info box should be shown.
     */
    private void showInfoBox() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean dismissedNotificationInfo = prefs.getBoolean(Constants.PREF_KEY_DISMISSED_NOTIFICATION_INFO, false);
        if (!dismissedNotificationInfo) {
            String title = getString(R.string.info_notifications_title);
            String message = getString(R.string.info_notifications_message);
            adapter.showInfoBox(title, message, Constants.PREF_KEY_DISMISSED_NOTIFICATION_INFO);
        }

        int counter = prefs.getInt(Constants.PREF_KEY_APPLICATION_LAUNCHES_COUNTER, 0);
        if (counter == 0) {
            return;
        }

        // show donation info at 8 or multiples of 20 app launches
        boolean dismissedDonationInfo = prefs.getBoolean(Constants.PREF_KEY_DISMISSED_DONATION_INFO, false);
        if (counter == 8 || counter % 20 == 0) {
            if (!dismissedDonationInfo) {
                showDonationInfo(prefs);
                return;
            }
        } else {
            prefs.edit().putBoolean(Constants.PREF_KEY_DISMISSED_DONATION_INFO, false).apply();
        }

        // show rating info at 15 app launches
        boolean dismissedRatingInfo = prefs.getBoolean(Constants.PREF_KEY_DISMISSED_RATING_INFO, false);
        if (counter == 15) {
            if (!dismissedRatingInfo) {
                showRatingInfo(prefs);
                return;
            }
        } else {
            prefs.edit().putBoolean(Constants.PREF_KEY_DISMISSED_RATING_INFO, false).apply();
        }

        // show edit mode info
        boolean dismissedEditModeInfo = prefs.getBoolean(Constants.PREF_KEY_DISMISSED_EDIT_MODE_INFO, false);
        if (!dismissedEditModeInfo) {
            String title = getString(R.string.info_edit_mode_title);
            String message = getString(R.string.info_edit_mode_message);
            adapter.showInfoBox(title, message, Constants.PREF_KEY_DISMISSED_EDIT_MODE_INFO);
        }
    }

    /**
     * Shows the donation info box.
     *
     * @param prefs shared preferences.
     */
    private void showDonationInfo(SharedPreferences prefs) {
        prefs.edit().putBoolean(Constants.PREF_KEY_DISMISSED_DONATION_INFO, false).apply();

        String title = getString(R.string.info_donation_title);
        String message = getString(R.string.info_donation_message);
        adapter.showInfoBox(title, message, Constants.PREF_KEY_DISMISSED_DONATION_INFO);
    }

    /**
     * Shows the rating info box.
     *
     * @param prefs shared preferences.
     */
    private void showRatingInfo(SharedPreferences prefs) {
        prefs.edit().putBoolean(Constants.PREF_KEY_DISMISSED_RATING_INFO, false).apply();

        String title = getString(R.string.info_rating_title);
        String message = getString(R.string.info_rating_message);
        adapter.showInfoBox(title, message, Constants.PREF_KEY_DISMISSED_RATING_INFO);
    }

    /**
     * Receive an event about the scraping progress and set the ProgressWheel accordingly.
     *
     * @param scrapeProgressEvent ScrapeProgressEvent
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(ScrapeProgressEvent scrapeProgressEvent) {
        if (ptrHeader != null && !scrapeProgressEvent.isScrapeForOverview()) {
            ptrHeader.increaseProgress(scrapeProgressEvent.getCurrentStep(), scrapeProgressEvent.getStepCount());
        }
    }

    /**
     * Receive a GradesEvent and add all grades to the adapter.
     *
     * @param gradesEvent - grades event
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(GradesEvent gradesEvent) {
        receivedGradesEvent = true;

        if (adapter != null) {
            adapter.setSemesterNumberMap(gradesEvent.getSemesterToSemesterNumberMap());
            adapter.setActualFirstSemester(gradesEvent.getActualFirstSemester());

            for(GradeEntry gradeEntry : gradesEvent.getGrades()) {
                GradeItem item = new GradeItem(gradeEntry);
                adapter.addGrade(item);
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

        getActivity().invalidateOptionsMenu();
    }

    /**
     * Receive a DeleteGradesEvent and deletes the GradeEntry.
     *
     * @param deleteGradeEvent DeleteGradeEvent
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(DeleteGradeEvent deleteGradeEvent) {
        if (adapter != null) {
            adapter.deleteGrade(new GradeItem(deleteGradeEvent.getGradeEntry()));
        }

        EventBus.getDefault().removeStickyEvent(deleteGradeEvent);
    }

    /**
     * Receive an ErrorEvent and display it to the user.
     *
     * @param errorEvent - ErrorEvent
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(ErrorEvent errorEvent) {
        if (ptrFrame != null && ptrHeader != null) {
            ptrHeader.setIsError(true);
            ptrFrame.refreshComplete();
        }

        UIHelper.displayErrorMessage(getView(), errorEvent, tryAgainListener, goToFaqListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_overview, menu);

        MenuItem editItem = menu.findItem(R.id.fragment_overview_edit);
        MenuItem saveItem = menu.findItem(R.id.fragment_overview_save);
        MenuItem restoreItem = menu.findItem(R.id.fragment_overview_restore);
        editItem.setVisible(receivedGradesEvent && !adapter.isEditModeEnabled());
        saveItem.setVisible(receivedGradesEvent && adapter.isEditModeEnabled());
        restoreItem.setVisible(receivedGradesEvent && adapter.isEditModeEnabled());

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragment_overview_edit:
                adapter.enableEditMode(true);
                fabAddGradeEntry.show(true);
                fabAddGradeEntry.setVisibility(View.VISIBLE);
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.fragment_overview_save:
                adapter.enableEditMode(false);
                fabAddGradeEntry.hide(true);
                fabAddGradeEntry.setVisibility(View.GONE);
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.fragment_overview_restore:
                showRestoreDialog();
                return true;
        }

        return false;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    private void showRestoreDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.dialog_restore_grade_entry_visibility);
        builder.setTitle(getString(R.string.dialog_restore_grade_entry_title));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                adapter.restoreVisibility();
                fabAddGradeEntry.hide(true);
                fabAddGradeEntry.setVisibility(View.GONE);
                getActivity().invalidateOptionsMenu();
            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean isEditModeEnabled() {
        return adapter.isEditModeEnabled();
    }

    public void disableEditMode() {
        adapter.enableEditMode(false);
        fabAddGradeEntry.hide();
        fabAddGradeEntry.setVisibility(View.GONE);
    }
}
