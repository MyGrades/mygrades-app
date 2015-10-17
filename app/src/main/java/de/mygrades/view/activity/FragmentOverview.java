package de.mygrades.view.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import de.mygrades.view.adapter.GradesRecyclerViewAdapter;
import de.mygrades.view.adapter.model.GradeItem;
import de.mygrades.view.decoration.GradesDividerItemDecoration;

/**
 * Fragment to show the overview of grades with a summary at the top.
 */
public class FragmentOverview extends Fragment {

    private RecyclerView rvGrades;
    private GradesRecyclerViewAdapter adapter;

    private MainServiceHelper mainServiceHelper;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        mainServiceHelper = new MainServiceHelper(getContext());

        // init recycler view
        initGradesRecyclerView(view);

        // init swipe to refresh layout
        initSwipeToRefresh(view);

        // register event bus
        EventBus.getDefault().register(this);

        // get all grades
        mainServiceHelper.getGradesFromDatabase();

        return view;
    }

    /**
     * Initialize the SwipeRefreshLayout.
     */
    private void initSwipeToRefresh(View rootView) {
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mainServiceHelper.scrapeForGrades(false);
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
     * Receive an GradesEvent and add all grades to the adapter.
     *
     * @param gradesEvent - grades event
     */
    public void onEventMainThread(GradesEvent gradesEvent) {
        if (adapter != null) {
            for(GradeEntry gradeEntry : gradesEvent.getGrades()) {
                GradeItem item = new GradeItem();
                item.setName(gradeEntry.getName());
                item.setHash(gradeEntry.getHash());

                Double creditPoints = gradeEntry.getCreditPoints();
                item.setCreditPoints(creditPoints == null ? null : creditPoints.floatValue());

                Double grade = gradeEntry.getGrade();
                item.setGrade(grade == null ? null : grade.floatValue());

                adapter.addGradeForSemester(item, gradeEntry.getSemesterNumber(), gradeEntry.getSemester());
            }

            // update the summary header
            adapter.updateSummary();
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Receive an ErrorEvent and display it to the user.
     *
     * @param errorEvent - ErrorEvent
     */
    public void onEventMainThread(ErrorEvent errorEvent) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        View.OnClickListener tryAgainListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swipeRefreshLayout.setRefreshing(true);
                mainServiceHelper.scrapeForGrades(false);
            }
        };

        switch (errorEvent.getType()) {
            case TIMEOUT:
                showSnackbar("Zeitüberschreitung", tryAgainListener, "Nochmal");
                break;
            case NO_NETWORK:
                showSnackbar("Keine Internetverbindung", tryAgainListener, "Nochmal");
                break;
            case GENERAL:
            default:
                String title = "Fehler beim Abrufen der Noten";
                String text = "Deine Noten konnten nicht abgerufen werden. \n" +
                        "Das kann verschiedene Gründe haben: \n" +
                        "1. Deine Zugangsdaten sind falsch. \n" +
                        "2. Probleme mit der Internetverbindung oder dem Server der Hochschule. \n" +
                        "3. Die Linkstruktur innerhalb deines Notensystems könnte sich geändert haben" +
                        " und so ist es zur Zeit nicht möglich deine Noten abzurufen.";

                if (getContext() != null) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(title)
                            .setMessage(text)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
        }
    }

    /**
     * Shows a Snackbar with a given text and action.
     *
     * @param text - text to show
     * @param action - OnClickListener
     * @param actionText - text for the OnClickListener
     */
    private void showSnackbar(String text, View.OnClickListener action, String actionText) {
        if (getView() != null) {
            Snackbar.make(getView(), text, Snackbar.LENGTH_LONG)
                    .setAction(actionText, action)
                    .show();
        }
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
