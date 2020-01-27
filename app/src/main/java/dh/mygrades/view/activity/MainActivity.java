package dh.mygrades.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.net.Uri;

import de.greenrobot.event.EventBus;
import dh.mygrades.BuildConfig;
import dh.mygrades.R;
import dh.mygrades.main.MainServiceHelper;
import dh.mygrades.main.events.InitialScrapingDoneEvent;
import dh.mygrades.main.events.LoginDataEvent;
import dh.mygrades.util.Constants;
import dh.mygrades.util.LogoutHelper;

/**
 * MainActivity uses a NavigationDrawer to switch its content between multiple fragments.
 *
 * On each startup it is checked whether the user is already logged in.
 * If not, he will be redirected to the SelectUniversityActivity.
 */
public class MainActivity extends AppCompatActivity implements ReplacableFragment {
    private SharedPreferences prefs;
    private MainServiceHelper mainServiceHelper;
    private DrawerLayout drawerLayout;

    private NavigationView navigationView;
    private LinearLayout llUserDataWrapper;
    private TextView tvMyGradesVersion;

    private static final String UNIVERSITY_NAME_STATE = "university_name_state";
    private static final String USERNAME_STATE = "username_state";
    private static final String TOOLBAR_LOGO_VISIBLE_STATE = "toolbar_icon_state";
    private static final String TOOLBAR_TITLE_STATE = "toolbar_title_state";

    private TextView tvUniversityName;
    private TextView tvUsername;

    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private FrameLayout flInitialScraping;
    private ImageView ivToolbarLogo;

    private boolean initialScrapingDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!checkLogin()) {
            goToUniversitySelection();
            return;
        }

        setContentView(R.layout.activity_main);

        mainServiceHelper = new MainServiceHelper(this);

        // init toolbar
        initToolbar(savedInstanceState);

        // setup navigation drawer
        setupDrawerContent(savedInstanceState);

        initialScrapingDone = isInitialScrapingDone();

        // check initial loading
        if (!initialScrapingDone) {
            // disable navigation drawer
            disableNavigationDrawer();

            // hide toolbar icon
            ivToolbarLogo.setAlpha(0f);

            // show initial scraping frame layout
            flInitialScraping = (FrameLayout) findViewById(R.id.fl_initial_scraping);
            flInitialScraping.setVisibility(View.VISIBLE);

            // show initial loading fragment. (its already shown, if savedInstanceState != null)
            if (savedInstanceState == null) {
                replaceFragment(R.id.fl_initial_scraping, new FragmentInitialScraping(), false);
            }
        } else {
            increaseApplicationLaunchesCounter(savedInstanceState);

            if (getIntent() != null && getIntent().getIntExtra(FragmentFaq.ARGUMENT_GO_TO_QUESTION, -1) >= 0) {
                // go to FAQs
                FragmentFaq fragmentFaq = new FragmentFaq();
                fragmentFaq.setArguments(getIntent().getExtras());
                replaceFragment(R.id.fl_content, fragmentFaq, false);
            } else if (savedInstanceState == null) {
                // set default fragment to overview of grades
                replaceFragment(R.id.fl_content, new FragmentOverview(), false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register to sticky events. Sticky events are necessary here,
        // to get notified even if the activity lost focus during the initial scraping.
        EventBus.getDefault().registerSticky(this);

        mainServiceHelper.getLoginDataFromDatabase();
    }

    /**
     * Increases the app launch counter if savedInstanceState is null.
     * The counter is used to show re-occurring info boxes.
     *
     * @param savedInstanceState saved instance state.
     */
    private void increaseApplicationLaunchesCounter(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            int count = prefs.getInt(Constants.PREF_KEY_APPLICATION_LAUNCHES_COUNTER, 0);
            count += 1;
            prefs.edit().putInt(Constants.PREF_KEY_APPLICATION_LAUNCHES_COUNTER, count).apply();
        }
    }

    /**
     * Initializes the toolbar.
     */
    private void initToolbar(Bundle savedInstanceState) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        ivToolbarLogo = (ImageView) findViewById(R.id.iv_mygrades_logo);

        if (savedInstanceState != null) {
            // set title
            String title = savedInstanceState.getString(TOOLBAR_TITLE_STATE, "");
            getSupportActionBar().setTitle(title);

            // set logo visibility
            int visibility = savedInstanceState.getBoolean(TOOLBAR_LOGO_VISIBLE_STATE) ? View.VISIBLE : View.GONE;
            ivToolbarLogo.setVisibility(visibility);
        }
    }

    /**
     * Checks if user is already logged in.
     *
     * @return true if user is logged in.
     */
    private boolean checkLogin() {
        long universityId = prefs.getLong(Constants.PREF_KEY_UNIVERSITY_ID, -1);
        return universityId > 0;
    }

    /**
     * Checks if the initial scraping of grades is done.
     *
     * @return true or false
     */
    private boolean isInitialScrapingDone() {
        return prefs.getBoolean(Constants.PREF_KEY_INITIAL_LOADING_DONE, false);
    }

    /**
     * Starts an intent to go to the SelectUniversityActivity.
     */
    private void goToUniversitySelection() {
        Intent intent = new Intent(this, SelectUniversityActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Receive an event when the initial scraping is done and replace fragments afterwards.
     *
     * @param initialScrapingDoneEvent InitialScrapingDoneEvent
     */
    public void onEventMainThread(InitialScrapingDoneEvent initialScrapingDoneEvent) {
        if (navigationView == null || drawerLayout == null || drawerToggle == null || toolbar == null) {
            return;
        }

        // show toolbar icon
        ivToolbarLogo.animate().alpha(1f).setDuration(1000);

        // replace initial scraping fragment with empty fragment (necessary for animation)
        replaceFragment(R.id.fl_initial_scraping, new FragmentEmptyDummy(), true);

        // replace fragment with custom transition
        replaceFragment(R.id.fl_content, new FragmentOverview(), true);

        // enable navigation drawer
        enableNavigationDrawer();

        // remove from sticky events
        EventBus.getDefault().removeStickyEvent(initialScrapingDoneEvent);
    }

    @Override
    public void replaceFragment(int resLayoutId, Fragment newFragment, boolean animate) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (animate) {
            transaction.setCustomAnimations(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);
        }

        transaction.replace(resLayoutId, newFragment, newFragment.getClass().getSimpleName());
        transaction.commit();

        // highlight menu item and set toolbar
        if (newFragment instanceof FragmentOverview) {
            navigationView.getMenu().getItem(0).setChecked(true);
            getSupportActionBar().setTitle("");
            ivToolbarLogo.setVisibility(View.VISIBLE);
        } else if (newFragment instanceof FragmentFaq) {
            navigationView.getMenu().getItem(3).setChecked(true);
            getSupportActionBar().setTitle(getString(R.string.toolbar_faq_title));
            ivToolbarLogo.setVisibility(View.GONE);
        } else if (newFragment instanceof FragmentReportError) {
            navigationView.getMenu().getItem(5).setChecked(true);
            getSupportActionBar().setTitle(getString(R.string.toolbar_report_error));
            ivToolbarLogo.setVisibility(View.GONE);
        } else if (newFragment instanceof FragmentStatistics) {
            navigationView.getMenu().getItem(1).setChecked(true);
            getSupportActionBar().setTitle(getString(R.string.toolbar_statistics));
            ivToolbarLogo.setVisibility(View.GONE);
        } else if (newFragment instanceof FragmentPrivacyPolicy) {
            navigationView.getMenu().getItem(6).setChecked(true);
            getSupportActionBar().setTitle(getString(R.string.toolbar_privacy_policy));
            ivToolbarLogo.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
            return;
        }

        if (!initialScrapingDone) {
            super.onBackPressed();
            return;
        }

        // check if current fragment is FragmentOverview
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment overview = fragmentManager.findFragmentByTag(FragmentOverview.class.getSimpleName());
        if (overview == null || !overview.isVisible()) {
            // go to overview (e.g. if user is currently at FAQs and presses back)
            replaceFragment(R.id.fl_content, new FragmentOverview(), false);
            return;
        } else {
            if (((FragmentOverview)overview).isEditModeEnabled()) {
                ((FragmentOverview)overview).disableEditMode();
                invalidateOptionsMenu();
                return;
            }
        }

        super.onBackPressed();
    }

    /**
     * Setup NavigationDrawer and initialize listener for clicked menu items.
     */
    private void setupDrawerContent(Bundle savedInstanceState) {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_drawer);

        // set header views
        View header = navigationView.getHeaderView(0);
        llUserDataWrapper = (LinearLayout) header.findViewById(R.id.ll_user_data_wrapper);
        tvMyGradesVersion = (TextView) header.findViewById(R.id.tv_mygrades_version);
        tvMyGradesVersion.setText(getString(R.string.tv_mygrades_version, BuildConfig.VERSION_NAME));
        tvUniversityName = (TextView) header.findViewById(R.id.tv_university_name);
        tvUsername = (TextView) header.findViewById(R.id.tv_username);

        // restore instance state
        if (savedInstanceState != null) {
            tvUniversityName.setText(savedInstanceState.getString(UNIVERSITY_NAME_STATE, ""));
            tvUsername.setText(savedInstanceState.getString(USERNAME_STATE, ""));
            llUserDataWrapper.setVisibility(View.VISIBLE);
        }

        // init drawer toggle (hamburger menu icon <-> arrow)
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                selectDrawerItem(menuItem);
                return false;
            }
        });
    }

    /**
     * Switch menu items and select fragment.
     *
     * @param menuItem clicked menu item
     */
    private void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment = null;
        Intent intent = null;

        switch(menuItem.getItemId()) {
            case R.id.nav_home:
                fragment = new FragmentOverview();
                break;
            case R.id.nav_faq:
                fragment = new FragmentFaq();
                break;
            case R.id.nav_report_error:
                fragment = new FragmentReportError();
                break;
            case R.id.nav_statistics:
                fragment = new FragmentStatistics();
                break;
            case R.id.nav_donation: 
                Uri uri = Uri.parse("https://www.buymeacoffee.com/DanielHabenicht");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_privacy_policy:
                fragment = new FragmentPrivacyPolicy();
                break;
            case R.id.nav_logout:
                LogoutHelper logoutHelper = new LogoutHelper(this);
                logoutHelper.showDialog();
                return;
            default:
                fragment = new FragmentOverview();
        }

        // replace current fragment if it is not the same as currently shown
        if (fragment != null && getSupportFragmentManager().findFragmentByTag(fragment.getClass().getSimpleName()) == null) {
            replaceFragment(R.id.fl_content, fragment, false);
        }

        // close the drawer
        drawerLayout.closeDrawers();
    }

    /**
     * Receive a LoginDataEvent with previously entered username and selected university.
     *
     * @param loginDataEvent LoginDataEvent
     */
    public void onEventMainThread(LoginDataEvent loginDataEvent) {
        tvUniversityName.setText(loginDataEvent.getUniversityName());
        tvUsername.setText(loginDataEvent.getUsername());
        llUserDataWrapper.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(UNIVERSITY_NAME_STATE, tvUniversityName.getText().toString());
        outState.putString(USERNAME_STATE, tvUsername.getText().toString());

        boolean isToolbarLogoVisible = ivToolbarLogo.getVisibility() == View.VISIBLE;
        outState.putBoolean(TOOLBAR_LOGO_VISIBLE_STATE, isToolbarLogoVisible);
        outState.putString(TOOLBAR_TITLE_STATE, getSupportActionBar().getTitle().toString());
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // sync the toggle state after onRestoreInstanceState has occurred
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // pass any configuration change to the drawer toggle
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Disable navigation drawer.
     */
    private void disableNavigationDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        drawerToggle.setDrawerIndicatorEnabled(false);
    }

    /**
     * Enable navigation drawer.
     */
    private void enableNavigationDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        drawerToggle.setDrawerIndicatorEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
