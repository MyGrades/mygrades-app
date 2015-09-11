package de.mygrades.view.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import de.mygrades.R;
import de.mygrades.database.DBHelper;
import de.mygrades.view.adapter.UniversityAdapter;

/**
 * Created by tilman on 11.09.15.
 */
public class SelectUniversityActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private UniversityAdapter universityAdapter;
    private ListView lvUniversities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_university);

        lvUniversities = (ListView) findViewById(R.id.lv_universities);
        universityAdapter = new UniversityAdapter(this, null);
        lvUniversities.setAdapter(universityAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
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
