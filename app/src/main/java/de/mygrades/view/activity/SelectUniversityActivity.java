package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.R;
import de.mygrades.database.dao.University;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.view.adapter.SimpleAdapter;
import de.mygrades.view.adapter.SimpleSectionedRecyclerViewAdapter;
import de.mygrades.view.decoration.DividerItemDecoration;
import de.mygrades.view.loader.UniversityLoader;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

/**
 * Activity which shows all universities.
 */
public class SelectUniversityActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<University>> {

    private RecyclerView rvUniversities;

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
        rvUniversities.setItemAnimator(new SlideInUpAnimator());
        rvUniversities.getItemAnimator().setAddDuration(500);
        rvUniversities.setHasFixedSize(true);

        // init loader
        getSupportLoaderManager().initLoader(0, null, this);

        // get all universities from server
        MainServiceHelper mainServiceHelper = new MainServiceHelper(this);
        mainServiceHelper.getUniversities();
    }

    @Override
    public Loader<List<University>> onCreateLoader(int id, Bundle args) {
        return new UniversityLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<University>> loader, List<University> data) {
        SimpleAdapter simpleAdapter = new SimpleAdapter(this, null);
        SimpleSectionedRecyclerViewAdapter sectionedAdapter = new SimpleSectionedRecyclerViewAdapter(this, simpleAdapter);

        // create sections
        List<SimpleSectionedRecyclerViewAdapter.Section> sections = new ArrayList<>();
        for(int i = 0; i < data.size(); i++) {
            String prefix = data.get(i).getName().substring(0, 1).toUpperCase();
            String prevPrefix = null;
            if (i > 0) {
                prevPrefix = data.get(i-1).getName().substring(0, 1).toUpperCase();
            }

            if (!prefix.equals(prevPrefix)) {
                sections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, ""+prefix));
            }
        }

        // set sections
        SimpleSectionedRecyclerViewAdapter.Section[] dummy = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        sectionedAdapter.setSections(sections.toArray(dummy));

        // set the adapter
        rvUniversities.setAdapter(sectionedAdapter);

        // add all items (animations!)
        for(int i = 0; i < data.size(); i++) {
            simpleAdapter.add(data.get(i), i);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<University>> loader) { }
}
