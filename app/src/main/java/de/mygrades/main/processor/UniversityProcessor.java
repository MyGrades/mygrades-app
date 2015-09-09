package de.mygrades.main.processor;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.mygrades.main.model.University;
import retrofit.RetrofitError;

/**
 * UniversityProcessor is responsible for university resources.
 * It makes rest calls and required inserts / updated to the local database.
 */
public class UniversityProcessor extends BaseProcessor {
    private static final String TAG = UniversityProcessor.class.getSimpleName();

    public UniversityProcessor(Context context) {
        super(context);
    }

    public void getUniversities() {
        List<University> universities = new ArrayList<>();

        // make synchronous rest call
        try {
            universities = restClient.getRestApi().getUniversities();
        } catch (RetrofitError e) {
            Log.e(TAG, "RetrofitError: " + e.getMessage());
        }

        // insert into database
        dbHelper.createUniversities(universities);
    }
}
