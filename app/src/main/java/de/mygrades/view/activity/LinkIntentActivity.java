package de.mygrades.view.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.util.List;

import de.mygrades.R;

/**
 * Listens to an intent-filter and shows different fragments.
 */
public class LinkIntentActivity extends AppCompatActivity implements ReplacableFragment {
    private static final String TAG = LinkIntentActivity.class.getSimpleName();


    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_link_intent);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // go to specified fragment
        evaluateIntentFragment(getIntent().getData());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button and just act like hardware back button
            case android.R.id.home:
                finish(); // removes activity from stack and goes to top of stack
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Evaluates which fragment should be shown and the options for that specific fragment.
     * @param intentData Uri of the intent
     */
    private void evaluateIntentFragment(Uri intentData) {
        Log.d(TAG, intentData.toString());

        // get path segments and evaluate fragment
        List<String> pathSegments = intentData.getPathSegments();
        if (pathSegments.size() > 0) {
            if ("faq".equals(pathSegments.get(0))) {
                // if a specific question for faq is set -> otherwise Fallback to faq
                if (pathSegments.size() > 1) {
                    int goToQuestion = Integer.parseInt(pathSegments.get(1));

                    FragmentFaq fragmentFaq = new FragmentFaq();
                    Bundle bundle = new Bundle();
                    bundle.putInt(FragmentFaq.ARGUMENT_GO_TO_QUESTION, goToQuestion);
                    fragmentFaq.setArguments(bundle);

                    replaceFragment(R.id.fl_content, fragmentFaq, false);
                    return;
                }
            } else if ("reporterror".equals(pathSegments.get(0))) {
                replaceFragment(R.id.fl_content, new FragmentReportError(), false);
                return;
            }
        }

        // Fallback: go to FAQs
        replaceFragment(R.id.fl_content, new FragmentFaq(), false);
    }

    @Override
    public void replaceFragment(int resLayoutId, Fragment newFragment, boolean animate) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (animate) {
            transaction.setCustomAnimations(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);
        }

        transaction.replace(resLayoutId, newFragment, newFragment.getClass().getSimpleName());
        transaction.commit();

        // set toolbar title
        if (newFragment instanceof FragmentFaq) {
            getSupportActionBar().setTitle(getString(R.string.toolbar_faq_title));
        } else if(newFragment instanceof FragmentReportError) {
            getSupportActionBar().setTitle(getString(R.string.toolbar_report_error));
        }
    }
}
