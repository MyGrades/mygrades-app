package de.mygrades.view.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.database.dao.GradeEntry;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.ErrorEvent;
import de.mygrades.main.events.GradesEvent;
import de.mygrades.util.Constants;
import de.mygrades.view.adapter.GradesRecyclerViewAdapter;
import de.mygrades.view.adapter.model.GradeItem;
import de.mygrades.view.decoration.GradesDividerItemDecoration;

/**
 * Fragment to show the overview of grades.
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

        // tvNoGradesFound = (TextView) view.findViewById(R.id.tv_no_grades_found);
        mainServiceHelper = new MainServiceHelper(getContext());

        // register event bus
        EventBus.getDefault().register(this);

        // init recycler view
        initGradesRecyclerView(view);

        // init swipe to refresh layout
        initSwipeToRefresh(view);

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
     * Initialize recycler view and add dummy items.
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

            adapter.updateSummary();
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Receive an Error Event and displays it to the user.
     *
     * @param errorEvent - error event
     */
    public void onEventMainThread(ErrorEvent errorEvent) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        String title;
        String text;

        switch (errorEvent.getType()) {
            case TIMEOUT:
                title = "Zeitüberschreitung bei der Anfrage";
                text = "Es hat zu lange gedauert mit dem Server deiner Hochschule zu kommunizieren. \n" +
                        "Das kann verschiedene Gründe haben: \n" +
                        "Deine Verbindung zum Internet ist nicht optimal. \n" +
                        "Das Notensystem deiner Hochschule ist überlastet.";
                break;
            case NO_NETWORK:
                title = "Keine Internetverbindung";
                text = "Du hast zur Zeit keine Verbindung zum Internet. Bitte versuche es erneut!";
                break;
            case GENERAL:
                title = "Fehler beim Abrufen der Noten";
                text = "Deine Noten konnten nicht abgerufen werden. \n" +
                        "Das kann verschiedene Gründe haben: \n" +
                        "1. Deine Zugangsdaten sind falsch. \n" +
                        "2. Probleme mit der Internetverbindung oder dem Server der Hochschule. \n" +
                        "3. Die Linkstruktur innerhalb deines Notensystems könnte sich geändert haben" +
                        " und so ist es zur Zeit nicht möglich deine Noten abzurufen.";
                break;
            default:
                title = "Fehler beim Abrufen der Noten";
                text = "Deine Noten konnten nicht abgerufen werden. \n" +
                        "Das kann verschiedene Gründe haben: \n" +
                        "1. Deine Zugangsdaten sind falsch. \n" +
                        "2. Probleme mit der Internetverbindung oder dem Server der Hochschule. \n" +
                        "3. Die Linkstruktur innerhalb deines Notensystems könnte sich geändert haben" +
                        " und so ist es zur Zeit nicht möglich deine Noten abzurufen.";
        }

        Context context = getContext();
        if (context != null) {
            new AlertDialog.Builder(context)
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

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
