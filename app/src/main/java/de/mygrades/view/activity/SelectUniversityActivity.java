package de.mygrades.view.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;

import de.mygrades.R;
import de.mygrades.database.DBHelper;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.view.adapter.UniversityAdapter;

/**
 * Activity which shows all universities.
 */
public class SelectUniversityActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private UniversityAdapter universityAdapter;
    private RecyclerView rvUniversities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_university);

        universityAdapter = new UniversityAdapter(this, null);
        rvUniversities = (RecyclerView) findViewById(R.id.rv_universities);
        rvUniversities.setLayoutManager(new LinearLayoutManager(rvUniversities.getContext()));
        rvUniversities.setAdapter(universityAdapter);

        getSupportLoaderManager().initLoader(0, null, this);

        // get all universities from server
        MainServiceHelper mainServiceHelper = new MainServiceHelper(this);
        mainServiceHelper.getUniversities();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        DBHelper dbHelper = new DBHelper(this);
        return dbHelper.getUniversityLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        universityAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        universityAdapter.swapCursor(null);
    }
}
