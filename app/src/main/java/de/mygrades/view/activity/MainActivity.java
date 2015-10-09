package de.mygrades.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import de.greenrobot.event.EventBus;
import de.mygrades.R;
import de.mygrades.main.MainServiceHelper;
import de.mygrades.main.events.InitialScrapingDoneEvent;
import de.mygrades.util.Constants;

/**
 * MainActivity uses a NavigationDrawer to switch between multiple fragments.
 */
public class MainActivity extends AppCompatActivity {
    private MainServiceHelper mainServiceHelper;
    private DrawerLayout drawerLayout;

    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private FrameLayout flInitialScraping;
    private ImageView ivToolbarLogo;

    private static final String TAG_FRAGMENT_INITIAL_SCRAPING = "tag_fragment_initial_scraping";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkLogin()) {
            goToUniversitySelection();
            return;
        }

        setContentView(R.layout.activity_main);
        mainServiceHelper = new MainServiceHelper(this);

        // init toolbar
        initToolbar();

        // setup navigation drawer
        setupDrawerContent();

        // register event bus
        EventBus.getDefault().register(this);

        // check initial loading
        if (!checkInitialScrapingDone()) {
            // disable navigation drawer
            disableNavigationDrawer();

            // hide toolbar icon
            ivToolbarLogo = (ImageView) findViewById(R.id.iv_mygrades_logo);
            ivToolbarLogo.setAlpha(0f);

            // show initial scraping frame layout
            flInitialScraping = (FrameLayout) findViewById(R.id.fl_initial_scraping);
            flInitialScraping.setVisibility(View.VISIBLE);

            // show initial loading fragment. (its already shown, if savedInstanceState != null)
            if (savedInstanceState == null) {
                replaceFragment(R.id.fl_initial_scraping, new FragmentInitialScraping(), false);
            }
        } else {
            // set default fragment to overview of grades
            if (savedInstanceState == null) {
                replaceFragment(R.id.fl_content, new FragmentOverview(), false);
                navigationView.getMenu().getItem(0).setChecked(true);
            }
        }
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
    }

    /**
     * Checks if user is already logged in.
     *
     * @return true if user is logged in.
     */
    private boolean checkLogin() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long universityId = prefs.getLong(Constants.PREF_KEY_UNIVERSITY_ID, -1);
        return universityId > 0;
    }

    /**
     * Checks if the initial loading of grades is done.
     *
     * @return true or false
     */
    private boolean checkInitialScrapingDone() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(Constants.PREF_KEY_INITIAL_LOADING_DONE, false);
    }

    /**
     * Starts the SelectUniversityActivity.
     */
    private void goToUniversitySelection() {
        Intent intent = new Intent(this, SelectUniversityActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Receive an event when the initial loading is done and replace fragments afterwards.
     *
     * @param initialScrapingDoneEvent
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
        navigationView.getMenu().getItem(0).setChecked(true);

        // enable navigation drawer
        enableNavigationDrawer();
    }

    /**
     * Replaces a fragment.
     *
     * @param resLayoutId frame layout id, whose content should be replaced
     * @param newFragment new fragment
     * @param animate true, if the replacement should be animated
     */
    private void replaceFragment( int resLayoutId, Fragment newFragment, boolean animate) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (animate) {
            transaction.setCustomAnimations(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);
        }

        transaction.replace(resLayoutId, newFragment);
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Setup navigation drawer an initialize listener for clicked menu items.
     */
    private void setupDrawerContent() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_drawer);

        // init drawer toggle (hamburger menu icon <-> arrow)
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                selectDrawerItem(menuItem);
                return true;
            }
        });
    }

    /**
     * Switch menu items and select fragment.
     *
     * @param menuItem clicked menu item
     */
    private void selectDrawerItem(MenuItem menuItem) {
        Fragment fragment;

        switch(menuItem.getItemId()) {
            case R.id.nav_home:
                fragment = new FragmentOverview();
                break;
            case R.id.nav_faq:
                fragment = new FragmentFaq();
                break;
            case R.id.nav_logout:
                logout();
                return;
            default:
                fragment = new FragmentOverview();
        }

        // set current fragment
        replaceFragment(R.id.fl_content, fragment, false);

        // highlight the selected item, update the title and close the drawer
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();
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

    /**
     * Logout user and goes to SelectUniversityActivity.
     */
    private void logout() {
        mainServiceHelper.logout();

        Intent intent = new Intent(this, SelectUniversityActivity.class);
        // set flags, so the user won't be able to go back to the main activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
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
}
