package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.view.adapter.UniversityAdapter;

/**
 * Activity which shows all universities.
 */
public class SelectUniversityActivity extends AppCompatActivity {

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

        // get all universities from server
        MainServiceHelper mainServiceHelper = new MainServiceHelper(this);
        mainServiceHelper.getUniversities();
    }
}
