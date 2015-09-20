package de.mygrades.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import de.mygrades.R;

/**
 * Activity to show the overview of grades.
 */
public class MainActivity extends AppCompatActivity {

    private Button btParse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btParse = (Button) findViewById(R.id.bt_parse);
        btParse.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "¯\\_(ツ)_/¯", Toast.LENGTH_LONG).show();
            }
        });
    }
}
