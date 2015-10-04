package de.mygrades.view.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        // setup navigation drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_drawer);
        setupDrawerContent();

        // init drawer toggle (hamburger menu icon <-> arrow)
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);

        // register event bus
        EventBus.getDefault().register(this);

        // check initial loading
        if (!checkInitialLoadingDone()) {
            // disable navigation drawer
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            drawerToggle.setDrawerIndicatorEnabled(false);

            // hide icon in toolbar
            ImageView iv = (ImageView) toolbar.findViewById(R.id.iv_mygrades_logo);
            iv.setAlpha(0f);

            // show initial loading fragment
            setFragment(FragmentInitialScraping.class);
        } else {
            // set default fragment to overview of grades
            if (savedInstanceState == null) {
                setFragment(FragmentOverview.class);
                navigationView.getMenu().getItem(0).setChecked(true);
            }
        }
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
    private boolean checkInitialLoadingDone() {
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

        // replace fragment with custom transition
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                       .setCustomAnimations(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top)
                       .replace(R.id.fl_content, new FragmentOverview())
                       .commit();
        navigationView.getMenu().getItem(0).setChecked(true);

        // enable navigation drawer
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        drawerToggle.setDrawerIndicatorEnabled(true);

        // show icon in toolbar
        ImageView iv = (ImageView) toolbar.findViewById(R.id.iv_mygrades_logo);
        iv.animate().alpha(1f).setDuration(1000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialize listener for clicked menu items.
     */
    private void setupDrawerContent() {
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
        Class fragmentClass;

        switch(menuItem.getItemId()) {
            case R.id.nav_home:
                fragmentClass = FragmentOverview.class;
                break;
            case R.id.nav_faq:
                fragmentClass = FragmentFaq.class;
                break;
            case R.id.nav_logout:
                logout();
                return;
            default:
                fragmentClass = FragmentOverview.class;
        }

        // set current fragment
        setFragment(fragmentClass);

        // highlight the selected item, update the title and close the drawer
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();
    }

    /**
     * Switches the current fragment with the provided fragment class.
     *
     * @param fragmentClass new fragment class
     */
    private void setFragment(Class<? extends Fragment> fragmentClass) {
        try {
            Fragment fragment = fragmentClass.newInstance();

            // insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fl_content, fragment).commit();
        } catch (Exception e) {
            // do nothing
        }
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
}
