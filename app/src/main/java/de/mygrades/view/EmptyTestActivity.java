package de.mygrades.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.mygrades.main.MainServiceHelper;

/**
 * Created by tilman on 09.09.15.
 */
public class EmptyTestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainServiceHelper mainServiceHelper = new MainServiceHelper(this);
        mainServiceHelper.getUniversities();
    }
}
