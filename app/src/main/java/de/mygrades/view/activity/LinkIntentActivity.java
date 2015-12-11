package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by jonastheis on 11.12.15.
 */
public class LinkIntentActivity extends AppCompatActivity {
    private static final String TAG = LinkIntentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, getIntent().getData().toString());
    }
}
